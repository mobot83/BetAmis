// k6 load test — validates HPA scale-up on prediction-service
// Usage:
//   kubectl port-forward -n betamis-dev svc/prediction-service 8082:8082
//   k6 run scripts/load-test-hpa.js
//
// Target: /q/metrics — Quarkus serialises all Prometheus counters/histograms
// on every call (no caching), making it genuinely CPU-intensive.
//
// With cpu.request=100m (dev override), HPA fires at 70m average CPU.
// 200 VUs with no sleep reliably pushes past that within the first scrape cycle.

import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 20  },  // warm-up JIT
    { duration: '1m',  target: 80  },  // ramp — HPA should trigger here
    { duration: '3m',  target: 80  },  // sustain — observe scale-up
    { duration: '30s', target: 0   },  // cool-down
  ],
  thresholds: {
    http_req_failed:   ['rate<0.05'],
    http_req_duration: ['p(95)<3000'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8082';

export default function () {
  // /q/metrics forces Quarkus to collect and serialise all Prometheus metrics
  // on every request — no caching, real CPU work per call.
  const res = http.get(`${BASE_URL}/q/metrics`);
  check(res, { 'status 200': (r) => r.status === 200 });
  sleep(0.05);
}
