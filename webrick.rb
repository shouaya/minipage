require 'webrick'

srv = WEBrick::HTTPServer.new({
  DocumentRoot:   './out',
  BindAddress:    '127.0.0.1',
  Port:           3000,
})

srv.start