package clegoues.util;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

/**
 * Delays shutdown of the JVM (e.g. due to Ctrl-C) to allow working threads to
 * clean up. This class should be used with the Java try-with-resources idiom:
 * <pre>
 *   // Give worker threads 5 seconds to shut down if canceled.
 *   try (ShutdownDelay sd = new ShutdownDelay(5000)) {
 *     for (Runnable task : tasks) {
 *       pool.execute(task);
 *     }
 *   }
 * </pre>
 * Each new ShutdownDelay registers a shutdown hook (see
 * {@link Runtime#addShutdownHook(Thread)}) on construction. Calling the
 * {@link #close()} method unregisters the hook. This allows the delay to be
 * active while tasks requiring cleanup are running, without unnecessarily
 * delaying shutdown after they complete.
 * 
 * @author jonathan
 */
public final class ShutdownDelay implements AutoCloseable {
	private static final Logger logger =
		Logger.getLogger( ShutdownDelay.class );
	
	/**
	 * Creates a new ShutdownDelay lasting the given number of milliseconds.
	 * 
	 * @param millis number of milliseconds to wait before allowing shutdown to
	 *               continue
	 */
	public ShutdownDelay( final long millis ) {
		shortCircuit = new AtomicBoolean( false );
		hook = new Thread() {
			@Override
			public void run() {
				long end = System.nanoTime() + millis * 1000000;
				while ( ! shortCircuit.get() ) {
					if ( end - System.nanoTime() <= 0 )
						break;
					try {
						Thread.sleep( 500 );
					} catch ( InterruptedException e ) {
						logger.warn( e.getMessage() );
						break;
					}
				}
			}
		};
		Runtime.getRuntime().addShutdownHook( hook );
	}

	@Override
	/**
	 * Cancels the ShutdownDelay. If the delay has already started, it will end
	 * early. Otherwise, it will be unregistered so that it is never invoked.
	 */
	public void close() {
		boolean wasTrue = shortCircuit.getAndSet( true );
		if ( !wasTrue ) {
			try {
				Runtime.getRuntime().removeShutdownHook( hook );
			} catch ( IllegalStateException e ) {
				// ignore: if it's already running, it will shut itself down
			}
		}
	}
	
	private final AtomicBoolean shortCircuit;
	private final Thread hook;
}
