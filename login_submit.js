var usernameInput = document.querySelector('input[placeholder="账号"]');
var passwordInput = document.querySelector('input[placeholder="密码"]');
var button = document.querySelector('button');

// Use native setter to trigger Vue reactivity
var nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
nativeInputValueSetter.call(usernameInput, 'admin');
usernameInput.dispatchEvent(new Event('input', { bubbles: true }));
nativeInputValueSetter.call(passwordInput, 'admin123');
passwordInput.dispatchEvent(new Event('input', { bubbles: true }));

// Now click the button
button.click();
'submitted'