package utility

import org.slf4j.LoggerFactory

trait Logging {
  private val logger = LoggerFactory.getLogger(getClass)

  def trace(message: => Any) = log(logger.isTraceEnabled, logger.trace, message)

  def debug(message: => Any) = log(logger.isDebugEnabled, logger.debug, message)

  def info(message: => Any) = log(logger.isInfoEnabled, logger.info, message)

  def warn(message: => Any) = log(logger.isWarnEnabled, logger.warn, message)

  def error(message: => Any) = log(logger.isErrorEnabled, logger.error, message)

  private def log(isEnabled: Boolean, log: String => Unit, message: => Any) = if (isEnabled) {
    if (message != null) log(message.toString)
  }
}