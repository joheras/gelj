// Molecular-weight markers shipped with GelJ (standardfiles/*.marker + standardfiles/info).
import type { Marker } from './types';

export const BUILTIN_MARKERS: Marker[] = [
  {
    id: 'builtin-100bp-plus',
    name: '100bp Plus DNA Ladder',
    mw: [3000, 2000, 1500, 1200, 1000, 900, 800, 700, 600, 500, 400, 300, 200, 100],
    refWidth: 46,
    refHeight: 605,
    refPositions: [36, 104, 161, 199, 246, 267, 290, 319, 348, 384, 423, 469, 523, 583],
    fitMethod: 12,
    builtin: true,
  },
  {
    id: 'builtin-lambda-pfg',
    name: 'Lambda Ladder PFG Marker',
    mw: [727.5, 679, 630.5, 582, 533.5, 485, 436.5, 388, 339.5, 291, 242.5, 194, 145.5, 97, 48.5],
    refWidth: 52,
    refHeight: 600,
    refPositions: [8, 30, 50, 72, 96, 122, 152, 182, 216, 252, 290, 336, 384, 440, 498],
    fitMethod: 9,
    builtin: true,
  },
  {
    id: 'builtin-low-range-pfg',
    name: 'Low Range PFG Marker',
    mw: [194, 145.5, 97, 48.5, 23.1, 9.42, 6.55, 4.36, 2.32, 2.03],
    refWidth: 42,
    refHeight: 600,
    refPositions: [42, 68, 106, 160, 196, 244, 278, 320, 450, 488],
    fitMethod: 12,
    builtin: true,
  },
  {
    id: 'builtin-midrange-pfg-i',
    name: 'MidRange PFG Marker I',
    mw: [242.5, 227.5, 209, 194, 179, 160.5, 145.5, 130.5, 112, 97, 82, 63.5, 48.5, 33.5, 15],
    refWidth: 56,
    refHeight: 640,
    refPositions: [24, 42, 72, 92, 118, 160, 188, 224, 276, 312, 354, 412, 446, 482, 546],
    fitMethod: 9,
    builtin: true,
  },
];
