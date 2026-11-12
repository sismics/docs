'use strict';
angular.module("ngLocale", [], ["$provide", function($provide) {
var PLURAL_CATEGORY = {ZERO: "zero", ONE: "one", TWO: "two", FEW: "few", MANY: "many", OTHER: "other"};
function getDecimals(n) {
  n = n + '';
  var i = n.indexOf('.');
  return (i == -1) ? 0 : n.length - i - 1;
}

function getVF(n, opt_precision) {
  var v = opt_precision;

  if (undefined === v) {
    v = Math.min(getDecimals(n), 3);
  }

  var base = Math.pow(10, v);
  var f = ((n * base) | 0) % base;
  return {v: v, f: f};
}

$provide.value("$locale", {
  "DATETIME_FORMATS": {
    "AMPMS": [
      "пре подне",
      "поподне"
    ],
    "DAY": [
      "недеља",
      "понедељак",
      "уторак",
      "среда",
      "четвртак",
      "петак",
      "субота"
    ],
    "ERANAMES": [
      "пре нове ере",
      "нове ере"
    ],
    "ERAS": [
      "п. н. е.",
      "н. е."
    ],
    "FIRSTDAYOFWEEK": 0,
    "MONTH": [
      "јануар",
      "фебруар",
      "март",
      "април",
      "мај",
      "јун",
      "јул",
      "август",
      "септембар",
      "октобар",
      "новембар",
      "децембар"
    ],
    "SHORTDAY": [
      "нед",
      "пон",
      "уто",
      "сре",
      "чет",
      "пет",
      "суб"
    ],
    "SHORTMONTH": [
      "јан",
      "феб",
      "мар",
      "апр",
      "мај",
      "јун",
      "јул",
      "авг",
      "сеп",
      "окт",
      "нов",
      "дец"
    ],
    "STANDALONEMONTH": [
      "јануар",
      "фебруар",
      "март",
      "април",
      "мај",
      "јун",
      "јул",
      "август",
      "септембар",
      "октобар",
      "новембар",
      "децембар"
    ],
    "WEEKENDRANGE": [
      5,
      6
    ],
    "fullDate": "EEEE, d. MMMM y.",
    "longDate": "d. MMMM y.",
    "medium": "d.MM.y. HH:mm:ss",
    "mediumDate": "d.MM.y.",
    "mediumTime": "HH:mm:ss",
    "short": "d.MM.yy. HH:mm",
    "shortDate": "d.MM.yy.",
    "shortTime": "HH:mm"
  },
  "NUMBER_FORMATS": {
    "CURRENCY_SYM": "дин.",
    "DECIMAL_SEP": ",",
    "GROUP_SEP": ".",
    "PATTERNS": [
      {
        "gSize": 3,
        "lgSize": 3,
        "maxFrac": 3,
        "minFrac": 0,
        "minInt": 1,
        "negPre": "-",
        "negSuf": "",
        "posPre": "",
        "posSuf": ""
      },
      {
        "gSize": 3,
        "lgSize": 3,
        "maxFrac": 2,
        "minFrac": 2,
        "minInt": 1,
        "negPre": "-",
        "negSuf": "\u00a0\u00a4",
        "posPre": "",
        "posSuf": "\u00a0\u00a4"
      }
    ]
  },
  "id": "sr",
  "localeID": "sr",
  "pluralCat": function(n, opt_precision) {  
    var i = n | 0;  
    var vf = getVF(n, opt_precision);  
    // Правило за ONE (нпр. 1, 21, 31, 101 порука)
    if (vf.v == 0 && i % 10 == 1 && i % 100 != 11 || vf.f % 10 == 1 && vf.f % 100 != 11) {  
      return PLURAL_CATEGORY.ONE;  
    }  
    // Правило за FEW (нпр. 2, 3, 4, 22, 23, 24 поруке)
    if (vf.v == 0 && i % 10 >= 2 && i % 10 <= 4 && (i % 100 < 12 || i % 100 > 14) || vf.f % 10 >= 2 && vf.f % 10 <= 4 && (vf.f % 100 < 12 || vf.f % 100 > 14)) {  
      return PLURAL_CATEGORY.FEW;  
    }  
    // Све остало (MANY/OTHER - нпр. 0, 5, 11, 20 порука)
    return PLURAL_CATEGORY.OTHER;
  }
});
}]);