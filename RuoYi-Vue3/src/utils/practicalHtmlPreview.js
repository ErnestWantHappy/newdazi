// 用户网页在无同源权限的沙箱中执行；外部资源、表单和上层窗口操作不开放。
export function buildPracticalHtmlPreview(source) {
  const policy = "default-src 'none'; script-src 'unsafe-inline'; style-src 'unsafe-inline'; img-src data: blob:; font-src data:; connect-src 'none'; frame-src 'none'; object-src 'none'; form-action 'none'; base-uri 'none'";
  const html = String(source || '');
  const declaration = html.match(/^(?:\s|<!--[^]*?-->)*<!doctype\s+html\b[^>]*>/i);
  const body = declaration ? html.slice(declaration[0].length) : html;
  // 安全策略必须早于用户内容，同时将标准声明保留在文档最前面。
  return `${declaration ? declaration[0] : ''}<meta http-equiv="Content-Security-Policy" content="${policy}"><meta charset="UTF-8">${body}`;
}
