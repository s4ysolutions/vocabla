package solutions.s4y.zio

import zio.logging.LogFilter.LogLevelByNameConfig
import zio.logging.LogFormat.*
import zio.logging.*
import zio.{LogLevel, Runtime, Trace}

val enclosingClass: LogFormat =
  LogFormat.make { (builder, trace, _, _, _, _, _, _, _) =>
    val parsed = Trace.toJava(trace)
    if (parsed ne null) {
      parsed match {
        case Some(clazz) =>
          builder.appendText(clazz.getClassName + ":" + clazz.getLineNumber)
        case None =>
          builder.appendText("not-available")
      }
    } else {
      builder.appendText("not-available")
    }
  }

val colorFormat =
  label("timestamp", timestamp.fixed(32)).color(LogColor.BLUE) |-|
    level.highlight |-|
    LogFormat.bracketed(label("thread", fiberId)).color(LogColor.YELLOW) |-|
    quoted(line).highlight |-|
    enclosingClass |-|
    LogFormat.allAnnotations |-|
    (space + label(
      "cause",
      cause
    ).highlight).filter(LogFilter.causeNonEmpty)

val consoleColorTraceLogger = Runtime.removeDefaultLoggers >>>
  consoleLogger(
    ConsoleLoggerConfig(colorFormat, LogLevelByNameConfig(LogLevel.Trace))
  )

val consoleColorDebugLogger = Runtime.removeDefaultLoggers >>>
  consoleLogger(
    ConsoleLoggerConfig(colorFormat, LogLevelByNameConfig(LogLevel.Debug))
  )
