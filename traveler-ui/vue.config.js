module.exports = {
    devServer: {
        proxy: {
            '/rrd': { // ride request dispatcher
                target: 'http://localhost:8384',
                ws: true,
                logLevel: 'debug',
                pathRewrite: {
                    "^/rrd": ""
                }
            }
        }
    }
};
