import * as React from "react";
import { Chart } from "chart.js/auto";
import ChartDatasourcePrometheusPlugin from 'chartjs-plugin-datasource-prometheus';
import 'chartjs-adapter-date-fns';
import { pt } from 'date-fns/locale';

Chart.registry.plugins.register(ChartDatasourcePrometheusPlugin);

function loadScrape(chartId, promQuery, title) {
  const ctx = document.getElementById(chartId);

  new Chart(ctx, {
    type: 'line',
    //plugins: [ChartDatasourcePrometheusPlugin],
    options: {
      responsive: false,
      animation: {
        duration: 0,
      },
      plugins: {
        'datasource-prometheus': {
          prometheus: {
            endpoint: "http://localhost:9090",
            baseURL: "/api/v1",   // default value
          },
          query: promQuery,
          timeRange: {
            type: 'relative',

            // from 12 hours ago to now
            start: -12 * 60 * 60 * 1000,
            end: 0,
            msUpdateInterval: 300000,
          },
          errorMsg: {
            font: "120px normal 'Helvetica Nueue'"
          }
        },
        title: {
          display: true,
          text: title,
          align: "center",
          font: {
            size: 24
          }
        }
      },
      scales: {
        x: {
          date: {
            locale: pt
          }
        }
      }
    },
  });
}

function PrometheusScrape({id}) {
  return (
    <div className="chart-container">
      <canvas id={id} style={{width: '70vw', height: '15vw'}}></canvas>
    </div>
  );
}
export {PrometheusScrape, loadScrape};