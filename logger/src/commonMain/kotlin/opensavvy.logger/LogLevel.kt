package opensavvy.logger

enum class LogLevel {
	// The order of the elements is important
	TRACE,
	DEBUG,
	INFO,
	WARN,
	ERROR,
	NONE,
	;

	/**
	 * `true` if [TRACE] messages should be printed.
	 */
	val trace = ordinal <= 0
	/**
	 * `true` if [DEBUG] messages should be printed.
	 */
	val debug = ordinal <= 1
	/**
	 * `true` if [INFO] messages should be printed.
	 */
	val info = ordinal <= 2
	/**
	 * `true` if [WARN] messages should be printed.
	 */
	val warn = ordinal <= 3
	/**
	 * `true` if [ERROR] messages should be printed.
	 */
	val error = ordinal <= 4

	companion object {
		/**
		 * The default log level.
		 *
		 * Whenever a new [Logger] is created, it uses this value to initialize its own [level][Logger.level].
		 * This value is mutable: for example to edit the log level for the whole program, you can use:
		 * ```kotlin
		 * import opensavvy.logger.LogLevel
		 *
		 * fun main() {
		 *     LogLevel.default = LogLevel.TRACE
		 *
		 *     // The rest of your code
		 * }
		 * ```
		 */
		var default = INFO
	}
}
