import { userUrl, partnerUrl, doorUrl, rsvUrl, userPopulateBody, partnerPopulateBody, doorPopulateBody, rsvPopulateBody } from "./Data"

export default Populate;

function Populate(Callback) {
  const end = (success) => {
    Callback(success);
  }

  userPop(end);
}


function userPop(end) {
  var req = new XMLHttpRequest();
  req.open("POST", userUrl + "insert", true);

  req.setRequestHeader("Content-Type", "application/json");

  req.onreadystatechange = function() {
    if (this.readyState === 4) {
      if (this.status === 200) {
        partnerPop(end);
      } else {
        end(false);
      }
    }
  }

  req.send(userPopulateBody);
}

function partnerPop(end) {
  var req = new XMLHttpRequest();
  req.open("POST", partnerUrl + "insert", true);

  req.setRequestHeader("Content-Type", "application/json");

  req.onreadystatechange = function() {
    if (this.readyState === 4) {
      if (this.status === 200) {
        doorPop(end);
      } else {
        end(false);
      }
    }
  }

  req.send(partnerPopulateBody);
}

function doorPop(end) {
  var req = new XMLHttpRequest();
  req.open("POST", doorUrl + "insert", true);

  req.setRequestHeader("Content-Type", "application/json");

  req.onreadystatechange = function() {
    if (this.readyState === 4) {
      if (this.status === 200) {
        rsvPop(end);
      } else {
        end(false);
      }
    }
  }

  req.send(doorPopulateBody);
}

function rsvPop(end) {
  var req = new XMLHttpRequest();
  req.open("POST", rsvUrl + "insert", true);

  req.setRequestHeader("Content-Type", "application/json");

  req.onreadystatechange = function() {
    if (this.readyState === 4) {
      end(this.status === 200);
    }
  }

  req.send(rsvPopulateBody);
}