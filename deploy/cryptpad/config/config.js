/* CryptPad 2026.5.1: 两域名隔离，平台通过 Integration API 嵌入。 */
module.exports = {
    httpUnsafeOrigin: 'http://office.xsedu.net.cn',
    httpSafeOrigin: 'http://office-sandbox.xsedu.net.cn',
    httpAddress: '0.0.0.0',
    httpPort: 3000,
    websocketPort: 3003,
    maxWorkers: 8,
    maxUploadSize: 50 * 1024 * 1024,
    filePath: '/cryptpad/datastore/',
    archivePath: '/cryptpad/data/archive',
    pinPath: '/cryptpad/data/pins',
    taskPath: '/cryptpad/data/tasks',
    blockPath: '/cryptpad/block',
    blobPath: '/cryptpad/blob',
    blobStagingPath: '/cryptpad/data/blobstage',
    decreePath: '/cryptpad/data/decrees',
    logPath: '/cryptpad/data/logs',
    logToStdout: true,
    logLevel: 'info',
    logFeedback: false,
    installMethod: 'docker-fixed-version'
};
