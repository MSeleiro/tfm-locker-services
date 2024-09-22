import { userUrl, partnerUrl, doorUrl, rsvUrl, userCleanBody, partnerCleanBody, doorCleanBody, rsvCleanBody } from "./Data"

export default Clean;

function Clean(Callback) {
  const end = (success) => {
    Callback(success);
  }

  rsvClean(end);
}


function rsvClean(end) {
  var req = new XMLHttpRequest();
  req.open("POST", rsvUrl + "delete", true);

  req.setRequestHeader("Content-Type", "application/json");

  req.onreadystatechange = function() {
    if (this.readyState === 4) {
      if (this.status === 200) {
        doorClean(end);
      } else {
        end(false);
      }
    }
  }

  req.send(rsvCleanBody);
}

function doorClean(end) {
  var req = new XMLHttpRequest();
  req.open("POST", doorUrl + "delete", true);

  req.setRequestHeader("Content-Type", "application/json");

  req.onreadystatechange = function() {
    if (this.readyState === 4) {
      if (this.status === 200) {
        partnerClean(end);
      } else {
        end(false);
      }
    }
  }

  req.send(doorCleanBody);
}

function partnerClean(end) {
  var req = new XMLHttpRequest();
  req.open("POST", partnerUrl + "delete", true);

  req.setRequestHeader("Content-Type", "application/json");

  req.onreadystatechange = function() {
    if (this.readyState === 4) {
      if (this.status === 200) {
        userClean(end);
      } else {
        end(false);
      }
    }
  }

  req.send(partnerCleanBody);
}

function userClean(end) {
  var req = new XMLHttpRequest();
  req.open("POST", userUrl + "delete", true);

  req.setRequestHeader("Content-Type", "application/json");

  req.onreadystatechange = function() {
    if (this.readyState === 4) {
      end(this.status === 200);
    }
  }

  req.send(userCleanBody);
}