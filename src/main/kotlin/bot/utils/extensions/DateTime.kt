package bot.utils.extensions

fun Long.humanizeMs(showMilliseconds: Boolean = false): String {
  var years = 0
  var months = 0
  var days = 0
  var hours = 0
  var minutes = 0
  var seconds = 0

  var ms = this

  if (ms >= 31556952000) {
    years += (ms / 31556952000).toInt()
    ms %= 31556952000
  }

  if (ms >= 2073600000) {
    months += (ms / 2073600000).toInt()
    ms %= 2073600000
  }

  if (ms >= 86400000) {
    days += (ms / 86400000).toInt()
    ms %= 86400000
  }

  if (ms >= 3600000) {
    hours += (ms / 3600000).toInt()
    ms %= 3600000
  }

  if (ms >= 60000) {
    minutes += (ms / 60000).toInt()
    ms %= 60000
  }

  if (ms >= 1000) {
    seconds += (ms / 1000).toInt()
    ms %= 1000
  }

  var str = " "

  if (years > 0) {
    str += "${years}y "
  }
  if (months > 0) {
    str += "${months}mo"
  }
  if (days > 0) {
    str += "${days}d"
  }
  if (hours > 0) {
    str += "${hours}h"
  }
  if (minutes > 0) {
    str += "${minutes}m"
  }
  if (seconds > 0) {
    str += "${seconds}s"
  }

  if (showMilliseconds) {
    if (ms > 0) {
      str += "${ms}ms"
    }
  }

  return str.trim()
}
