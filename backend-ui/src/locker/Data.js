const codeBody = "{ \"code\": \"password\" }";

const userPopulateBody = "{ \"user_id\": \"c5a6d53b-dfa3-4d41-8f08-4983207f9eaa\", \"user_name\": \"ya\" }";

const partnerPopulateBody = "{ \"ptnr_id\": \"c5a6d53b-dfa3-4d41-8f08-4983207f9eab\", \"ptnr_name\": \"ya-partner\" }";

const doorPopulateBody = "{ \"door_id\": 1, \"door_type\": \"SMALL\", \"door_ptnr\": \"c5a6d53b-dfa3-4d41-8f08-4983207f9eab\" }";

const rsvPopulateBody = "{ \"rsv_id\": \"9e94add2-5c31-4f3b-8b5e-c8b8cd3c5a39\", \"rsv_status\": \"ARRIVING\", \"rsv_user\": \"c5a6d53b-dfa3-4d41-8f08-4983207f9eaa\", \"rsv_door\": 1, \"rsv_dcode\": \"5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8\", \"rsv_lcode\": \"0b14d501a594442a01c6859541bcb3e8164d183d32937b851835442f69d5c94e\", \"rsv_ccode\": \"6cf615d5bcaac778352a8f1f3360d23f02f34ec182e259897fd6ce485d7870d4\" }";

const userCleanBody = "{ \"user_id\": \"c5a6d53b-dfa3-4d41-8f08-4983207f9eaa\", \"user_name\": \"yo\" }";

const partnerCleanBody = "{ \"ptnr_id\": \"c5a6d53b-dfa3-4d41-8f08-4983207f9eab\", \"ptnr_name\": \"yo-partner\" }";

const doorCleanBody = "{ \"door_id\": 1, \"door_type\": \"LARGE\", \"door_ptnr\": \"c5a6d53b-dfa3-4d41-8f08-4983207f9eab\" }";

const rsvCleanBody = "{ \"rsv_id\": \"9e94add2-5c31-4f3b-8b5e-c8b8cd3c5a39\", \"rsv_status\": \"ARRIVING\", \"rsv_user\": \"c5a6d53b-dfa3-4d41-8f08-4983207f9eaa\", \"rsv_door\": 1, \"rsv_dcode\": \"!5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8\", \"rsv_lcode\": \"0b14d501a594442a01c6859541bcb3e8164d183d32937b851835442f69d5c94e\", \"rsv_ccode\": \"6cf615d5bcaac778352a8f1f3360d23f02f34ec182e259897fd6ce485d7870d4\" }";

const codeUrl = "/api/function/locker-screen-io";

const userUrl = "/api/function/locker-user-manager?action=";

const partnerUrl = "/api/function/locker-partner-manager?action=";

const doorUrl = "/api/function/locker-door-manager?action=";

const rsvUrl = "/api/function/locker-reservation-manager?action=";

const deployedUrl = '/api/system/functions';

export {
  codeBody, 
  userPopulateBody, 
  partnerPopulateBody, 
  doorPopulateBody,
  rsvPopulateBody,
  userCleanBody,
  partnerCleanBody,
  doorCleanBody,
  rsvCleanBody,
  codeUrl,
  userUrl,
  partnerUrl,
  doorUrl,
  rsvUrl,
  deployedUrl
};