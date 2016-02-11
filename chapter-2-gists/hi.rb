require 'sinatra'
require 'octokit'

set :bind, '0.0.0.0'
set :views, "."

helpers do
  def h(text)
    Rack::Utils.escape_html(text)
  end
end

get '/:username' do |username|
  gists = Octokit.gists username, :per_page => 5
  tuples = []
  gists.each do |g|
    g[:files].fields.each do |f|
      data = g[:files][f].rels[:raw].get.data
      tuples << [ f, data ]
    end
  end
  erb :index, locals: { :tuples => tuples, username: username }
end

get '/' do 
  "Try adding a GitHub username to the URL..."
end

get "/favicon.ico" do
end
