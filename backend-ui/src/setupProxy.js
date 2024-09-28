const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
  app.use(
    '/api', // <-- or whatever path segment precedes your server side routes
    createProxyMiddleware({
      target: 'http://192.168.1.4:8080', // <-- or whatever your proxy endpoint is
      changeOrigin: true,
      pathRewrite: {
        '^/api/': '/', // remove base path
      },
      auth: process.env.REACT_APP_MIDDLEWARE_AUTH
    })
  );
};