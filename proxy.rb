require 'rubygems'
require 'sinatra'
require 'sinatra/cookies'
require 'net/http'

set :public_folder, File.dirname(__FILE__) + '/out'
set :cookie_options, { domain: 'localhost', path: '/' }

post '/profile/edit' do
  content_type :json
  uri = URI('http://localhost:9000/profile/edit')
  req = Net::HTTP::Post.new(uri)
  req["Content-Type"] = 'application/json'
  req["Cookie"] = 'uid=test; token=test'
  req.body = request.body.read
  res = Net::HTTP.start(uri.hostname, uri.port) do |http|
    resp = http.request(req)
	p resp.body
  end
end