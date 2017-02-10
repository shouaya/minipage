require 'rubygems'
require 'sinatra'
require 'sinatra/cookies'
require 'net/http'

set :public_folder, File.dirname(__FILE__) + './out'
set :cookie_options, { domain: 'localhost', path: '/' }

get '/' do 
	p File.dirname(__FILE__) + './out'
end

#user
post '/profile/edit' do
  content_type :json
  uri = URI('http://localhost:9000/profile/edit')
  req = Net::HTTP::Post.new(uri)
  #req.basic_auth 'matt', 'secret'
  req["Content-Type"] = 'application/json'
  req["Cookie"] = 'uid=test; token=test'
  req.body = request.body.read
  res = Net::HTTP.start(uri.hostname, uri.port) do |http|
    resp = http.request(req)
	p resp.body
  end
end

#admin
get '/profile/:id' do |id|
  content_type :json
  uri = URI('http://localhost:9000/profile/' + id)
  req = Net::HTTP::Get.new(uri)
  req["Cookie"] = 'uid=test; token=test'
  req["Content-Type"] = 'application/json'
  req.body = request.body.read
  res = Net::HTTP.start(uri.hostname, uri.port) do |http|
    resp = http.request(req)
	p resp.body
  end
end

get '/line/user' do
  content_type :json
  uri = URI('http://localhost:9000/line/user?' + request.query_string)
  req = Net::HTTP::Get.new(uri)
  req["Cookie"] = 'uid=test; token=test'
  req["Content-Type"] = 'application/json'
  req.body = request.body.read
  res = Net::HTTP.start(uri.hostname, uri.port) do |http|
    resp = http.request(req)
	p resp.body
  end
end


get '/line/message' do
  content_type :json
  uri = URI('http://localhost:9000/line/message?' + request.query_string)
  req = Net::HTTP::Get.new(uri)
  req["Cookie"] = 'uid=test; token=test'
  req["Content-Type"] = 'application/json'
  req.body = request.body.read
  res = Net::HTTP.start(uri.hostname, uri.port) do |http|
    resp = http.request(req)
	p resp.body
  end
end

get '/line/reply' do
  content_type :json
  uri = URI('http://localhost:9000/line/reply?' + request.query_string)
  req = Net::HTTP::Get.new(uri)
  req["Cookie"] = 'uid=test; token=test'
  req["Content-Type"] = 'application/json'
  req.body = request.body.read
  res = Net::HTTP.start(uri.hostname, uri.port) do |http|
    resp = http.request(req)
	p resp.body
  end
end

post '/line/reply' do
  content_type :json
  uri = URI('http://localhost:9000/line/reply')
  req = Net::HTTP::Post.new(uri)
  req["Cookie"] = 'uid=test; token=test'
  req["Content-Type"] = 'application/json'
  req.body = request.body.read
  res = Net::HTTP.start(uri.hostname, uri.port) do |http|
    resp = http.request(req)
	p resp.body
  end
end

post '/file/upload' do
  content_type :json
  uri = URI('http://localhost:9000/file/upload')
  req = Net::HTTP::Post.new(uri)
  req["Cookie"] = 'uid=test; token=test'
  req["content-type"] = request.content_type
  req.body = request.body.read
  res = Net::HTTP.start(uri.hostname, uri.port) do |http|
    resp = http.request(req)
	p resp.body
  end
end
