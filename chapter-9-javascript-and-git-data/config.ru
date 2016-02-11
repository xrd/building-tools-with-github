app = proc do |env|
  Rack::Directory.new('.').call(env)
end

run app
