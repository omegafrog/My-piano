import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const SEARCH_BACKEND = (__ENV.SEARCH_BACKEND || 'es').toLowerCase();
const SEARCH_SENTENCE = __ENV.SEARCH_SENTENCE || 'title';
const INSTRUMENT = __ENV.INSTRUMENT || 'GUITAR_ACOUSTIC';
const DIFFICULTY = __ENV.DIFFICULTY || 'MEDIUM';
const GENRE = __ENV.GENRE || 'BGM';
const PAGE = __ENV.PAGE || '0';
const SIZE = __ENV.SIZE || '20';
const DETAIL_SAMPLE_INDEX = Number(__ENV.DETAIL_SAMPLE_INDEX || '0');

export const searchLatency = new Trend('search_latency_ms', true);
export const detailLatency = new Trend('detail_latency_ms', true);
export const journeyLatency = new Trend('journey_latency_ms', true);
export const searchErrors = new Counter('search_errors');
export const detailErrors = new Counter('detail_errors');

export const options = {
  scenarios: {
    search_browse: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.RATE || '20'),
      timeUnit: '1s',
      duration: __ENV.DURATION || '2m',
      preAllocatedVUs: Number(__ENV.PRE_ALLOCATED_VUS || '20'),
      maxVUs: Number(__ENV.MAX_VUS || '100'),
    },
  },
  thresholds: {
    search_latency_ms: ['p(95)<500'],
    detail_latency_ms: ['p(95)<300'],
    journey_latency_ms: ['p(95)<800'],
  },
};

function searchUrl() {
  const params = [
    `searchSentence=${encodeURIComponent(SEARCH_SENTENCE)}`,
    `instrument=${encodeURIComponent(INSTRUMENT)}`,
    `difficulty=${encodeURIComponent(DIFFICULTY)}`,
    `genre=${encodeURIComponent(GENRE)}`,
    `searchBackend=${encodeURIComponent(SEARCH_BACKEND)}`,
    `page=${encodeURIComponent(PAGE)}`,
    `size=${encodeURIComponent(SIZE)}`,
  ].join('&');
  return `${BASE_URL}/api/v1/sheet-post?${params}`;
}

export default function () {
  const journeyStart = Date.now();

  const searchRes = http.get(searchUrl(), {
    tags: { endpoint: 'sheet-post-search', backend: SEARCH_BACKEND },
  });
  searchLatency.add(searchRes.timings.duration, { backend: SEARCH_BACKEND });

  const searchOk = check(searchRes, {
    'search status is 200': (res) => res.status === 200,
  });
  if (!searchOk) {
    searchErrors.add(1, { backend: SEARCH_BACKEND });
    sleep(1);
    return;
  }

  const searchJson = searchRes.json();
  const items = searchJson?.data?.content || searchJson?.data?.items || [];
  const firstId = items.length > DETAIL_SAMPLE_INDEX ? items[DETAIL_SAMPLE_INDEX]?.id : null;

  if (firstId != null) {
    const detailRes = http.get(`${BASE_URL}/api/v1/sheet-post/${firstId}`, {
      tags: { endpoint: 'sheet-post-detail', backend: SEARCH_BACKEND },
    });
    detailLatency.add(detailRes.timings.duration, { backend: SEARCH_BACKEND });
    if (detailRes.status !== 200) {
      detailErrors.add(1, { backend: SEARCH_BACKEND });
    }
    check(detailRes, {
      'detail status is 200': (res) => res.status === 200,
    });
  }

  journeyLatency.add(Date.now() - journeyStart, { backend: SEARCH_BACKEND });
  sleep(1);
}
