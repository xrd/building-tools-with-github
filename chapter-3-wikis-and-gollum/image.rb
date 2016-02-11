require 'sinatra'
require 'gollum-lib'
require 'tempfile'
require 'zip/zip'
require 'rugged'

set :bind, '0.0.0.0'

def index( message=nil )
  response = File.read(File.join('.', 'index.html'))
  response.gsub!( "<!-- message -->\n", "<h2>Received and unpacked #{message}</h2>" ) if message
  response
end

wiki = Gollum::Wiki.new ARGV.shift
get '/' do
  index()
end

post '/unpack' do
  @repo = Rugged::Repository.new('.')
  @index = Rugged::Index.new
  files = []
  dir = File.join "images", @repo.head.target

  zip = params[:zip][:tempfile]
  
  Zip::ZipFile.open( zip ) { |zipfile|
    zipfile.each do |f|
      contents = zipfile.read( f.name )
      filename = f.name.split( File::SEPARATOR ).pop
      if contents and filename and filename =~ /(png|jp?g|gif)$/i
        write_file_to_repo contents, filename, dir # Write the file
        files << filename
      end
    end
    write_review_file files, dir # write out a review file
    build_commit() # Build a commit from the new files
  }
  index( params[:zip][:filename] )
end  

def write_review_file( files, dir )
  review_filename = "Review.md"
  contents = "## Review Images\n\n"
  files.each do |f|
    contents += "### #{f} \n[[#{dir}/#{f}]]\n\n"
  end
  contents += "[Prior revision (only when viewing history)](#{@repo.head.target})\n\n"

  File.write review_filename, contents
  oid = @repo.write( contents, :blob )
  @index.add(:path => review_filename, :oid => oid, :mode => 0100644)
end

def get_credentials
  contents = File.read File.join( ENV['HOME'], ".gitconfig" )
  @email = $1 if contents =~ /email = (.+)$/
  @name = $1 if contents =~ /name = (.+)$/
end

def build_commit
  get_credentials()
  options = {}
  options[:tree] = @index.write_tree(@repo)
  options[:author] = { :email => @email, :name => @name, :time => Time.now }
  options[:committer] = { :email => @email, :name => @name, :time => Time.now }
  options[:message] = params[:message]
  options[:parents] = @repo.empty? ? [] : [ @repo.head.target ].compact
  options[:update_ref] = 'HEAD'

  Rugged::Commit.create(@repo, options)
  
end

def write_file_to_repo( contents, filename, dir )
  Dir.mkdir "images" unless File.exists? "images"
  Dir.mkdir dir unless File.exists? dir
  full_name = File.join dir, filename
  File.write full_name, contents
  oid = @repo.write( contents, :blob )
  @index.add(:path => full_name, :oid => oid, :mode => 0100644)
end

