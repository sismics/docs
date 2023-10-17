/**
 * Angular directive/filter/service for formatting date so that it displays how long ago the given time was compared to now.
 * @version v0.4.5 - 2017-04-17
 * @link https://github.com/yaru22/angular-timeago
 * @author Brian Park <yaru22@gmail.com>
 * @license MIT License, http://www.opensource.org/licenses/MIT
 */
'use strict';

angular.module('yaru22.angular-timeago', []);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['ca_ES'] = {
    prefixAgo: 'fa',
    prefixFromNow: 'd\'aquí',
    suffixAgo: null,
    suffixFromNow: null,
    seconds: 'menys d\'un minut',
    minute: 'prop d\'un minut',
    minutes: '%d minuts',
    hour: 'prop d\'una hora',
    hours: 'prop de %d hores',
    day: 'un dia',
    days: '%d dies',
    month: 'prop d\'un mes',
    months: '%d mesos',
    year: 'prop d\'un any',
    years: '%d anys',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['el'] = {
    prefixAgo: null,
    prefixFromNow: null,
    suffixAgo: 'πριν',
    suffixFromNow: 'από τώρα',
    seconds: 'λιγότερο από ένα λεπτό',
    minute: 'περίπου ένα λεπτό',
    minutes: '%d λεπτά',
    hour: 'περίπου μια ώρα',
    hours: 'περίπου %d ώρες',
    day: 'μια μέρα',
    days: '%d μέρες',
    month: 'περίπου ένα μήνα',
    months: '%d μήνες',
    year: 'περίπου ένα χρόνο',
    years: '%d χρόνια',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {

  /**
   * Czech language uses 2 different versions for future based on the digit being
   * lower than 5 or not.
   */
  function resolvePastAndFuture(past, future, future5) {
    return function(d, millis) {
      var isFuture = millis < 0;

      if (!isFuture) {
        return past;
      } else {
        if (d <= 4) {
          return future;
        } else {
          return future5;
        }
      }
    };
  }

  timeAgoSettings.strings['cs_CZ'] = {
    prefixAgo: 'prěd',
    prefixFromNow: 'za',
    suffixAgo: null,
    suffixFromNow: null,

    //the below works for past
    seconds: resolvePastAndFuture('méně než minutou', 'méne než minutu', 'méne než minutu'),
    minute: resolvePastAndFuture('minutou', 'minutu', 'minutu'),
    minutes: resolvePastAndFuture('%d minutami', '%d minuty', '%d minút'),
    hour: resolvePastAndFuture('hodinou', 'hodinu', 'hodinu'),
    hours: resolvePastAndFuture('%d hodinama', '%d hodiny', '%d hodin'),
    day: resolvePastAndFuture('dnem', 'den', 'den'),
    days: resolvePastAndFuture('%d dny', '%d dny', '%d dnů'),
    month: resolvePastAndFuture('měsícem', 'měsíc', 'měsíc'),
    months: resolvePastAndFuture('%d měsíci', '%d měsíce', '%d měsíců'),
    year: resolvePastAndFuture('rokem', 'rok', 'rok'),
    years: resolvePastAndFuture('%d lety', '%d roky', '%d let'),
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['da_DK'] = {
    prefixAgo: null,
    prefixFromNow: null,
    suffixAgo: 'siden',
    suffixFromNow: null,
    seconds: 'mindre end et minut',
    minute: 'omkring et minut',
    minutes: '%d minuter',
    hour: 'omkring en time',
    hours: 'omkring %d timer',
    day: 'en dag',
    days: '%d dage',
    month: 'omkring en m\xe5ned',
    months: '%d m\xe5neder',
    year: 'omkring et \xe5r',
    years: '%d \xe5r',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['de'] = {
    prefixAgo: 'vor',
    prefixFromNow: 'in',
    suffixAgo: null,
    suffixFromNow: null,
    seconds: 'weniger als einer Minute',
    minute: 'ca. einer Minute',
    minutes: '%d Minuten',
    hour: 'ca. einer Stunde',
    hours: 'ca. %d Stunden',
    day: 'einem Tag',
    days: '%d Tagen',
    month: 'ca. einem Monat',
    months: '%d Monaten',
    year: 'ca. einem Jahr',
    years: '%d Jahren',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['en_US'] = {
    prefixAgo: null,
    prefixFromNow: null,
    suffixAgo: 'ago',
    suffixFromNow: 'from now',
    seconds: 'less than a minute',
    minute: 'about a minute',
    minutes: '%d minutes',
    hour: 'about an hour',
    hours: 'about %d hours',
    day: 'a day',
    days: '%d days',
    month: 'about a month',
    months: '%d months',
    year: 'about a year',
    years: '%d years',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['es'] = {
    prefixAgo: 'hace',
    prefixFromNow: 'dentro de',
    suffixAgo: null,
    suffixFromNow: null,
    seconds: 'menos de un minuto',
    minute: 'un minuto',
    minutes: '%d minutos',
    hour: 'una hora',
    hours: '%d horas',
    day: 'un día',
    days: '%d días',
    month: 'un mes',
    months: '%d meses',
    year: 'un año',
    years: '%d años',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['es_ES'] = {
    prefixAgo: 'hace',
    prefixFromNow: 'dentro de',
    suffixAgo: null,
    suffixFromNow: null,
    seconds: 'menos de un minuto',
    minute: 'un minuto',
    minutes: '%d minutos',
    hour: 'una hora',
    hours: '%d horas',
    day: 'un día',
    days: '%d días',
    month: 'un mes',
    months: '%d meses',
    year: 'un año',
    years: '%d años',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['es_LA'] = {
    prefixAgo: 'hace',
    prefixFromNow: 'en',
    suffixAgo: null,
    suffixFromNow: null,
    seconds: 'menos de un minuto',
    minute: 'un minuto',
    minutes: '%d minutos',
    hour: 'una hora',
    hours: '%d horas',
    day: 'un día',
    days: '%d días',
    month: 'un mes',
    months: '%d meses',
    year: 'un año',
    years: '%d años',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['fr'] = {
    prefixAgo: 'il y a',
    prefixFromNow: 'dans',
    suffixAgo: null,
    suffixFromNow: null,
    seconds: 'moins d\'une minute',
    minute: 'environ une minute',
    minutes: '%d minutes',
    hour: 'environ une heure',
    hours: 'environ %d heures',
    day: 'un jour',
    days: '%d jours',
    month: 'environ un mois',
    months: '%d mois',
    year: 'environ un an',
    years: '%d ans',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['he_IL'] = {
    prefixAgo: null,
    prefixFromNow: null,
    suffixAgo: 'לפני',
    suffixFromNow: 'מעכשיו',
    seconds: 'פחות מדקה',
    minute: 'כדקה',
    minutes: '%d דקות',
    hour: 'כשעה',
    hours: 'כ %d שעות',
    day: 'יום',
    days: '%d ימים',
    month: 'כחודש',
    months: '%d חודשים',
    year: 'כשנה',
    years: '%d שנים',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['hu_HU'] = {
    prefixAgo: null,
    prefixFromNow: null,
    suffixAgo: null,
    suffixFromNow: null,
    seconds: 'kevesebb mint egy perce',
    minute: 'körülbelül egy perce',
    minutes: '%d perce',
    hour: 'körülbelül egy órája',
    hours: 'körülbelül %d órája',
    day: 'egy napja',
    days: '%d napja',
    month: 'körülbelül egy hónapja',
    months: '%d hónapja',
    year: 'körülbelül egy éve',
    years: '%d éve',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['it'] = {
    prefixAgo: null,
    prefixFromNow: null,
    suffixAgo: 'fa',
    suffixFromNow: 'da ora',
    seconds: 'meno di un minuto',
    minute: 'circa un minuto',
    minutes: '%d minuti',
    hour: 'circa un\' ora',
    hours: 'circa %d ore',
    day: 'un giorno',
    days: '%d giorni',
    month: 'circa un mese',
    months: '%d mesi',
    year: 'circa un anno',
    years: '%d anni',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['nl_NL'] = {
    prefixAgo: null,
    prefixFromNow: 'over',
    suffixAgo: 'geleden',
    suffixFromNow: 'vanaf nu',
    seconds: 'een paar seconden',
    minute: 'ongeveer een minuut',
    minutes: '%d minuten',
    hour: 'een uur',
    hours: '%d uur',
    day: 'een dag',
    days: '%d dagen',
    month: 'een maand',
    months: '%d maanden',
    year: 'een jaar',
    years: '%d jaar',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['pl_PL'] = {
    prefixAgo: null,
    prefixFromNow: null,
    suffixAgo: 'temu',
    suffixFromNow: 'od teraz',
    seconds: 'mniej niż minuta',
    minute: 'około minuty',
    minutes: '%d minut',
    hour: 'około godziny',
    hours: 'około %d godzin',
    day: 'dzień',
    days: '%d dni',
    month: 'około miesiąca',
    months: '%d miesięcy',
    year: 'około roku',
    years: '%d lat',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['pl'] = {
    prefixAgo: null,
    prefixFromNow: null,
    suffixAgo: 'temu',
    suffixFromNow: 'od teraz',
    seconds: 'mniej niż minuta',
    minute: 'około minuty',
    minutes: '%d minut',
    hour: 'około godziny',
    hours: 'około %d godzin',
    day: 'dzień',
    days: '%d dni',
    month: 'około miesiąca',
    months: '%d miesięcy',
    year: 'około roku',
    years: '%d lat',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['pt_BR'] = {
    prefixAgo: null,
    prefixFromNow: 'daqui a',
    suffixAgo: 'atrás',
    suffixFromNow: null,
    seconds: 'menos de um minuto',
    minute: 'cerca de um minuto',
    minutes: '%d minutos',
    hour: 'cerca de uma hora',
    hours: 'cerca de %d horas',
    day: 'um dia',
    days: '%d dias',
    month: 'cerca de um mês',
    months: '%d meses',
    year: 'cerca de um ano',
    years: '%d anos',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['ru'] = {
    prefixAgo: null,
    prefixFromNow: null,
    suffixAgo: 'назад',
    suffixFromNow: 'с текущего момента',
    seconds: 'менее минуты',
    minute: 'около минуты',
    minutes: '%d мин.',
    hour: 'около часа',
    hours: 'около %d ч.',
    day: 'день',
    days: '%d дн.',
    month: 'около месяца',
    months: '%d мес.',
    year: 'около года',
    years: '%d года',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['ru'] = {
    prefixAgo: null,
    prefixFromNow: null,
    suffixAgo: 'назад',
    suffixFromNow: null,
    seconds: 'меньше минуты',
    minute: 'около минуты',
    minutes: '%d мин.',
    hour: 'около часа',
    hours: 'около %d час.',
    day: 'день',
    days: '%d дн.',
    month: 'около месяца',
    months: '%d мес.',
    year: 'около года',
    years: '%d г.',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['sv_SE'] = {
    prefixAgo: null,
    prefixFromNow: 'om',
    suffixAgo: 'sen',
    suffixFromNow: null,
    seconds: 'mindre än en minut',
    minute: 'cirka en minut',
    minutes: '%d minuter',
    hour: 'cirka en timme',
    hours: 'cirka %d timmar',
    day: 'en dag',
    days: '%d dagar',
    month: 'cirka en månad',
    months: '%d månader',
    year: 'cirka ett år',
    years: '%d år',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['tr_TR'] = {
    prefixAgo: null,
    prefixFromNow: null,
    suffixAgo: 'önce',
    suffixFromNow: 'şu andan itibaren',
    seconds: 'bir dakikadan daha az',
    minute: 'bir dakika gibi',
    minutes: '%d dakika',
    hour: 'bir saat gibi',
    hours: '%d saat gibi',
    day: 'bir gün',
    days: '%d gün',
    month: 'bir ay gibi',
    months: '%d ay',
    year: 'bir yıl gibi',
    years: '%d yıl',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['zh_CN'] = {
    wordSeparator: '',
    prefixAgo: null,
    prefixFromNow: null,
    suffixAgo: '前',
    suffixFromNow: '后',
    seconds: '1分钟',
    minute: '1分钟',
    minutes: '%d分钟',
    hour: '1小时',
    hours: '%d小时',
    day: '1天',
    days: '%d天',
    month: '1个月',
    months: '%d个月',
    year: '1年',
    years: '%d年',
    numbers: []
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').config(["timeAgoSettings", function(timeAgoSettings) {
  timeAgoSettings.strings['zh_TW'] = {
    wordSeparator: '',
    prefixAgo: null,
    prefixFromNow: null,
    suffixAgo: '前',
    suffixFromNow: '後',
    seconds: '少於一分鐘',
    minute: '一分鐘',
    minutes: '%d分鐘',
    hour: '一小時',
    hours: '%d小時',
    day: '一日',
    days: '%d日',
    month: '一個月',
    months: '%d個月',
    year: '一年',
    years: '%d年',
    numbers: [
      '零', '一', '二', '三', '四', '五', '六', '七', '八', '九', '十',
      '十一', '十二', '十三', '十四', '十五', '十六', '十七', '十八', '十九', '二十',
      '廿一', '廿二', '廿三', '廿四', '廿五', '廿六', '廿七', '廿八', '廿九', '三十',
      '卅一', '卅二', '卅三', '卅四', '卅五', '卅六', '卅七', '卅八', '卅九', '四十',
      '卌一', '卌二', '卌三', '卌四', '卌五', '卌六', '卌七', '卌八', '卌九', '五十',
      '五十一', '五十二', '五十三', '五十四', '五十五', '五十六', '五十七', '五十八', '五十九', '六十',
      '六十一', '六十二', '六十三', '六十四', '六十五', '六十六', '六十七', '六十八', '六十九', '七十',
      '七十一', '七十二', '七十三', '七十四', '七十五', '七十六', '七十七', '七十八', '七十九', '八十',
      '八十一', '八十二', '八十三', '八十四', '八十五', '八十六', '八十七', '八十八', '八十九', '九十',
      '九十一', '九十二', '九十三', '九十四', '九十五', '九十六', '九十七', '九十八', '九十九', '一百',
    ]
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').factory('nowTime', ["$interval", "timeAgo", "timeAgoSettings", function($interval, timeAgo, timeAgoSettings) {
  var nowTime;

  function updateTime() {
    nowTime = Date.now();
  }
  updateTime();
  $interval(updateTime, timeAgoSettings.refreshMillis);

  return function() {
    return nowTime;
  };
}]);

'use strict';

angular.module('yaru22.angular-timeago').constant('timeAgoSettings', {
  refreshMillis: 1000,
  allowFuture: false,
  overrideLang: null,
  fullDateAfterSeconds: null,
  strings: {},
  breakpoints: {
    secondsToMinute: 45, // in seconds
    secondsToMinutes: 90, // in seconds
    minutesToHour: 45, // in minutes
    minutesToHours: 90, // in minutes
    hoursToDay: 24, // in hours
    hoursToDays: 42, // in hours
    daysToMonth: 30, // in days
    daysToMonths: 45, // in days
    daysToYear: 365, // in days
    yearToYears: 1.5 // in year
  }
});

'use strict';

angular.module('yaru22.angular-timeago').directive('timeAgo', ["timeAgo", "nowTime", function(timeAgo, nowTime) {
  return {
    scope: {
      fromTime: '@',
      format: '@'
    },
    restrict: 'EA',
    link: function(scope, elem) {
      var fromTime;

      // Track changes to fromTime
      scope.$watch('fromTime', function() {
        fromTime = timeAgo.parse(scope.fromTime);
      });

      // Track changes to time difference
      scope.$watch(function() {
        return nowTime() - fromTime;
      }, function(value) {
        angular.element(elem).text(timeAgo.inWords(value, fromTime, scope.format));
      });
    }
  };
}]);

'use strict';
/*global moment */

angular.module('yaru22.angular-timeago').factory('timeAgo', ["$filter", "timeAgoSettings", function($filter, timeAgoSettings) {
  var service = {};

  service.inWords = function(distanceMillis, fromTime, format, timezone) {

    var fullDateAfterSeconds = parseInt(timeAgoSettings.fullDateAfterSeconds, 10);

    if (!isNaN(fullDateAfterSeconds)) {
      var fullDateAfterMillis = fullDateAfterSeconds * 1000;
      if ((distanceMillis >= 0 && fullDateAfterMillis <= distanceMillis) ||
        (distanceMillis < 0 && fullDateAfterMillis >= distanceMillis)) {
        if (format) {
          return $filter('date')(fromTime, format, timezone);
        }
        return fromTime;
      }
    }

    var overrideLang = timeAgoSettings.overrideLang;
    var documentLang = document.documentElement.lang;
    var sstrings = timeAgoSettings.strings;
    var lang, $l;

    if (typeof sstrings[overrideLang] !== 'undefined') {
      lang = overrideLang;
      $l = sstrings[overrideLang];
    } else if (typeof sstrings[documentLang] !== 'undefined') {
      lang = documentLang;
      $l = sstrings[documentLang];
    } else {
      lang = 'en_US';
      $l = sstrings[lang];
    }

    var prefix = $l.prefixAgo;
    var suffix = $l.suffixAgo;
    if (timeAgoSettings.allowFuture) {
      if (distanceMillis < 0) {
        prefix = $l.prefixFromNow;
        suffix = $l.suffixFromNow;
      }
    }

    var seconds = Math.abs(distanceMillis) / 1000;
    var minutes = seconds / 60;
    var hours = minutes / 60;
    var days = hours / 24;
    var years = days / 365;

    function substitute(stringOrFunction, number) {
      number = Math.round(number);
      var string = angular.isFunction(stringOrFunction) ?
        stringOrFunction(number, distanceMillis) : stringOrFunction;
      var value = ($l.numbers && $l.numbers[number]) || number;
      return string.replace(/%d/i, value);
    }

    var breakpoints = timeAgoSettings.breakpoints;
    var words = seconds < breakpoints.secondsToMinute && substitute($l.seconds, seconds) ||
      seconds < breakpoints.secondsToMinutes && substitute($l.minute, 1) ||
      minutes < breakpoints.minutesToHour && substitute($l.minutes, minutes) ||
      minutes < breakpoints.minutesToHours && substitute($l.hour, 1) ||
      hours < breakpoints.hoursToDay && substitute($l.hours, hours) ||
      hours < breakpoints.hoursToDays && substitute($l.day, 1) ||
      days < breakpoints.daysToMonth && substitute($l.days, days) ||
      days < breakpoints.daysToMonths && substitute($l.month, 1) ||
      days < breakpoints.daysToYear && substitute($l.months, days / 30) ||
      years < breakpoints.yearToYears && substitute($l.year, 1) ||
      substitute($l.years, years);

    var separator = $l.wordSeparator === undefined ? ' ' : $l.wordSeparator;
    if (lang === 'he_IL') {
      return [prefix, suffix, words].join(separator).trim();
    } else {
      return [prefix, words, suffix].join(separator).trim();
    }
  };

  service.parse = function(input) {
    if (input instanceof Date) {
      return input;
    } else if ((typeof moment !== 'undefined') && moment.isMoment(input)) {
      return input.toDate();
    } else if (angular.isNumber(input)) {
      return new Date(input);
    } else if (/^\d+$/.test(input)) {
      return new Date(parseInt(input, 10));
    } else {
      var s = (input || '').trim();
      s = s.replace(/\.\d+/, ''); // remove milliseconds
      s = s.replace(/-/, '/').replace(/-/, '/');
      s = s.replace(/T/, ' ').replace(/Z/, ' UTC');
      s = s.replace(/([\+\-]\d\d)\:?(\d\d)/, ' $1$2'); // -04:00 -> -0400
      return new Date(s);
    }
  };

  return service;
}]);

'use strict';

angular.module('yaru22.angular-timeago').filter('timeAgo', ["nowTime", "timeAgo", function(nowTime, timeAgo) {
  function timeAgoFilter(value, format, timezone) {
    var fromTime = timeAgo.parse(value);
    var diff = nowTime() - fromTime;
    return timeAgo.inWords(diff, fromTime, format, timezone);
  }
  timeAgoFilter.$stateful = true;
  return timeAgoFilter;
}]);