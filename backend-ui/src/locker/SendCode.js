import { codeUrl, codeBody } from "./Data"

function SendCode(Callback) {
  const req = new XMLHttpRequest();
  req.open("POST", codeUrl, true);

  req.setRequestHeader("Content-Type", "application/json");

  req.onreadystatechange = function() {
    if (this.readyState === 4) {
      Callback(this.status === 200);
    }
  }

  req.send(codeBody);
}
    
export default SendCode;