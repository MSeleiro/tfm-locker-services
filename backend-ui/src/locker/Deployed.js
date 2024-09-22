import { deployedUrl } from './Data'

function Deployed(Callback) {
  var req = new XMLHttpRequest();
  req.open("GET", deployedUrl, true);

  req.setRequestHeader("Content-Type", "application/json");

  req.onreadystatechange = function() {
    if (this.readyState === 4) {
      if (this.response[0] === '[') {
        Callback(JSON.parse(this.response).length);
      } else {
        console.log(this.response);
      }
    }
  };

  req.send();
}

export default Deployed;