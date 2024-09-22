import * as React from 'react';
import Chart from 'chart.js/auto';
import { CategoryScale } from "chart.js";
import Populate from "./locker/Populate";
import Clean from "./locker/Clean";
import SendCode from "./locker/SendCode";
import { PrometheusScrape, loadScrape } from "./charts/PrometheusScrape";
import { PopulateBtn, CleanDBBtn, CodeBtn } from './utils/Buttons';
import Deployed from './locker/Deployed';
import Grid from '@mui/material/Grid';

import './App.css';

Chart.register(CategoryScale);

function App() {
  const [deployed, setDeployed] = React.useState(0);

  React.useEffect(() => {
    const interval = setInterval(() => Deployed(setDeployed), 60000);
    return () => {
      clearInterval(interval);
    };
  }, []);

  Deployed(setDeployed);

  document.body.onload = () => {
    loadScrape('gccChart', 'sum by (job) (go_gc_duration_seconds)', 'Garbage Collection pause duration (in seconds)')
    loadScrape('residentMem', 'process_resident_memory_bytes', 'Resident Memory (in bytes)')
  }

  return (
    <div className="App">
      <header className="App-header">
        <Grid container spacing={2} height={"100vh"} >
          <Grid item xs={2} backgroundColor={"#1b1e29"} alignContent={'center'} position={'relative'}>
            <PopulateBtn action={Populate}/>
            <CleanDBBtn action={Clean}/>
            <CodeBtn action={SendCode}/>
          </Grid>
          <Grid item xs={10} alignContent={'center'}>
            <div>
              <h2 style={{ color: "#666", position: 'absolute', top: '0%' }}>Deployed Functions: {deployed}</h2>
              <PrometheusScrape id="gccChart"/>
              <p></p>
              <PrometheusScrape id="residentMem"/>
            </div>
          </Grid>
        </Grid>
      </header>
    </div>
  );
}

export default App;
