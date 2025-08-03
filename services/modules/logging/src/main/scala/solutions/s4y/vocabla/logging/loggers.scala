package solutions.s4y.vocabla.logging

import zio.logging.LogFilter.LogLevelByNameConfig
import zio.logging.LogFormat.{
  cause,
  fiberId,
  label,
  level,
  line,
  quoted,
  space,
  timestamp
}
import zio.logging.{
  ConsoleLoggerConfig,
  LogColor,
  LogFilter,
  LogFormat,
  consoleLogger
}
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

val consoleColorTraceLogger = Runtime.removeDefaultLoggers >>>
  consoleLogger(
    ConsoleLoggerConfig(
      label("timestamp", timestamp.fixed(32)).color(LogColor.BLUE) |-|
        level.highlight |-|
        LogFormat.bracketed(label("thread", fiberId)).color(LogColor.YELLOW) |-|
        quoted(line).highlight |-|
        enclosingClass |-|
        LogFormat.allAnnotations |-|
        (space + label(
          "cause",
          cause
        ).highlight).filter(LogFilter.causeNonEmpty),
      LogLevelByNameConfig(LogLevel.Trace)
    )
  )
