import * as React from 'react';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import { green, red } from '@mui/material/colors';

function PopulateBtn({ action }) {
    const [loadPop, setLoadPop] = React.useState(false);
    const [loadEnd, setLoadEnd] = React.useState(false);
    const [popSuccess, setPopSuccess] = React.useState(false);
    
    const loadStyle = {
      ...(loadEnd && {
            bgcolor: popSuccess ? green[500] : red[500],
            '&:hover': {
            bgcolor: popSuccess ? green[700] : red[700],
          },
      }),
    };

    const populateEnd = (success) => {
        if (success) {
          setPopSuccess(true);
        }
        setLoadEnd(true);
        setLoadPop(false);
    } 

    const doPopulate = () => {
        if (!loadPop) {
            setPopSuccess(false);
            setLoadEnd(false);
            setLoadPop(true);
            action(populateEnd)
        }
    };

    return (
        <div style={{position: 'absolute', top: '5%', left: '11.5%'}}>
            <Button 
                size="medium" 
                variant="contained"
                disabled={loadPop}
                onClick={() => { doPopulate(); }} 
                sx={loadStyle}
            >
                Populate DB
            </Button>
            
            {loadPop && (
                <CircularProgress
                size={24}
                sx={{
                    color: green[500],
                    position: 'absolute',
                    top: '30%',
                    left: '43%',
                }}
                />
            )}
        </div>
    );
}

function CleanDBBtn({ action }) {
  const [loadClean, setLoadClean] = React.useState(false);
  const [cleanEnd, setCleanEnd] = React.useState(false);
  const [cleanSuccess, setCleanSuccess] = React.useState(false);
  
  const cleanStyle = {
    ...(cleanEnd && {
          bgcolor: cleanSuccess ? green[500] : red[500],
          '&:hover': {
          bgcolor: cleanSuccess ? green[700] : red[700],
        },
    }),
  };

  const cleanDBEnd = (success) => {
      if (success) {
        setCleanSuccess(true);
      }
      setCleanEnd(true);
      setLoadClean(false);
  } 

  const doClean = () => {
      if (!loadClean) {
          setCleanSuccess(false);
          setCleanEnd(false);
          setLoadClean(true);
          action(cleanDBEnd)
      }
  };

  return (
      <div style={{position: 'absolute', top: '5%', left: '60%'}}>
          <Button 
              size="medium" 
              variant="contained"
              disabled={loadClean}
              onClick={() => { doClean(); }} 
              sx={cleanStyle}
          >
              Clean DB
          </Button>
          
          {loadClean && (
              <CircularProgress
              size={24}
              sx={{
                  color: green[500],
                  position: 'absolute',
                  top: '30%',
                  left: '40%',
              }}
              />
          )}
      </div>
  );
}

function CodeBtn({ action }) {
    const [loadCode, setLoadCode] = React.useState(false);
    const [loadCodeEnd, setLoadCodeEnd] = React.useState(false);
    const [codeSuccess, setCodeSuccess] = React.useState(false);

    const codeStyle = {
      ...(loadCodeEnd && {
          bgcolor: codeSuccess ? green[500] : red[500],
          '&:hover': {
          bgcolor: codeSuccess ? green[700] : red[700],
        },
      }),
    }; 

    const sendCodeEnd = (success) => {
        if (success) {
          setCodeSuccess(true);
        }
        setLoadCodeEnd(true);
        setLoadCode(false);
    }

    const doSendCode = () => {
        if (!loadCode) {
            setCodeSuccess(false);
            setLoadCodeEnd(false);
            setLoadCode(true);
            action(sendCodeEnd)
        }
    };

    return (
        <div>
            <Button 
              size="large" 
              variant="contained" 
              disabled={loadCode}
              onClick={() => { doSendCode(); }}
              sx={codeStyle}
            >
              Send Code
            </Button>

            {loadCode && (
              <CircularProgress
                size={24}
                sx={{
                  color: green[500],
                  position: 'absolute',
                  top: '49.6%',
                  left: '48%',
                }}
              />
            )}
        </div>
    );
}

export { PopulateBtn, CleanDBBtn, CodeBtn }