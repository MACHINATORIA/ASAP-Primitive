package asap.primitive.thread;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import asap.primitive.math.NumberHelper;

public class ThreadHelper {

    public static final int DEFAULT_THREAD_POOL_KEEPALIVE = 15000;

    public static interface AsapThreadFactoryListener {

        public void threadCreated( long id,
                                   String name,
                                   boolean daemon,
                                   int priority );

        public void uncaughtException( Thread thread,
                                       Throwable exception );
    }

    public static class AsapThreadFactory implements ThreadFactory {

        public static final String          DEFAULT_THREAD_NAME_PREFIX = "AsapThread";

        public static final ThreadGroup     DEFAULT_THREAD_GROUP       = new ThreadGroup( DEFAULT_THREAD_NAME_PREFIX );

        protected ThreadGroup               group;

        protected String                    namePrefix;

        protected boolean                   daemon;

        protected int                       priority;

        protected AsapThreadFactoryListener listener;

        protected int                       sequence;

        protected UncaughtExceptionHandler  uncaughtExceptionHandler;

        public AsapThreadFactory( ) {
            this( null,
                  null );
        }

        public AsapThreadFactory( String namePrefix ) {
            this( namePrefix,
                  null );
        }

        public AsapThreadFactory( String namePrefix,
                                  AsapThreadFactoryListener listener ) {
            this( null,
                  namePrefix,
                  false,
                  Thread.NORM_PRIORITY,
                  listener );
        }

        public AsapThreadFactory( ThreadGroup group,
                                  String namePrefix,
                                  boolean daemon,
                                  int priority,
                                  AsapThreadFactoryListener listener ) {
            this.group = ( group != null ) ? group
                                           : DEFAULT_THREAD_GROUP;
            this.namePrefix = ( namePrefix != null ) ? namePrefix
                                                     : "AsapThread";
            this.daemon = daemon;
            this.priority = NumberHelper.fitRange( priority,
                                                   Thread.MIN_PRIORITY,
                                                   Thread.MAX_PRIORITY );
            this.listener = listener;
            //
            this.sequence = 1;
            this.uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler( ) {

                @Override
                public void uncaughtException( Thread thread,
                                               Throwable exception ) {
                    if ( AsapThreadFactory.this.listener != null ) {
                        try {
                            AsapThreadFactory.this.listener.uncaughtException( thread,
                                                                               exception );
                        }
                        catch ( Throwable e ) {
                            e.printStackTrace( );
                        }
                    }
                }
            };
        }

        @Override
        public Thread newThread( Runnable runnable ) {
            Thread tmpThread = new Thread( this.group,
                                           runnable,
                                           String.format( "%s-%04d",
                                                          this.namePrefix,
                                                          this.sequence ) );
            this.sequence += 1;
            tmpThread.setPriority( this.priority );
            tmpThread.setDaemon( this.daemon );
            tmpThread.setUncaughtExceptionHandler( this.uncaughtExceptionHandler );
            if ( this.listener != null ) {
                try {
                    this.listener.threadCreated( tmpThread.getId( ),
                                                 tmpThread.getName( ),
                                                 this.daemon,
                                                 tmpThread.getPriority( ) );
                }
                catch ( Throwable e ) {
                    e.printStackTrace( );
                }
            }
            return tmpThread;
        }
    }

    public static interface AsapThreadPoolListener {

        public void abortingTask( Thread thread,
                                  Runnable runnable );

        public void beforeExecute( Thread thread,
                                   Runnable runnable );

        public void afterExecute( Runnable runnable,
                                  Throwable throwabe );

        public void terminated( );
    }

    public static class AsapFixedThreadPoolExecutor extends ThreadPoolExecutor {

        protected AsapThreadPoolListener listener;

        public AsapFixedThreadPoolExecutor( int coreThreads,
                                            int maxThreads,
                                            int queueSize,
                                            int keepAlive,
                                            String namePrefix,
                                            AsapThreadPoolListener listener ) {
            this( coreThreads,
                  maxThreads,
                  queueSize,
                  keepAlive,
                  new AsapThreadFactory( namePrefix,
                                         null ),
                  listener );
        }

        public AsapFixedThreadPoolExecutor( int coreThreads,
                                            int maxThreads,
                                            int queueSize,
                                            int keepAlive,
                                            ThreadFactory factory,
                                            AsapThreadPoolListener listener ) {
            super( coreThreads,
                   maxThreads,
                   keepAlive,
                   TimeUnit.MILLISECONDS,
                   new ArrayBlockingQueue< Runnable >( queueSize ),
                   factory,
                   new ThreadPoolExecutor.AbortPolicy( ) );
            this.listener = listener;
            // this.allowCoreThreadTimeOut( true );
            this.prestartCoreThread( );
        }

        @Override
        protected void beforeExecute( Thread thread,
                                      Runnable runnable ) {
            super.beforeExecute( thread,
                                 runnable );
            if ( this.isShutdown( ) ) {
                if ( this.listener != null ) {
                    this.listener.abortingTask( thread,
                                                runnable );
                }
                thread.interrupt( );
            }
            else {
                if ( this.listener != null ) {
                    this.listener.beforeExecute( thread,
                                                 runnable );
                }
            }
        }

        @Override
        protected void afterExecute( Runnable runnable,
                                     Throwable throwabe ) {
            super.afterExecute( runnable,
                                throwabe );
            if ( this.listener != null ) {
                this.listener.afterExecute( runnable,
                                            throwabe );
            }
        }

        @Override
        protected void terminated( ) {
            super.terminated( );
            if ( this.listener != null ) {
                this.listener.terminated( );
            }
        }

        public boolean awaitTermination( long timeoutMiliseconds )
            throws InterruptedException {
            return this.awaitTermination( timeoutMiliseconds,
                                          TimeUnit.MILLISECONDS );
        }

        @Override
        public boolean awaitTermination( long timeout,
                                         TimeUnit unit )
            throws InterruptedException {
            boolean tmpResult = false;
            if ( this.isShutdown( ) ) {
                long tmpTimeoutMilis = TimeUnit.MILLISECONDS.convert( timeout,
                                                                      unit );
                long tmpTargetTime = ( System.currentTimeMillis( ) + tmpTimeoutMilis );
                while ( System.currentTimeMillis( ) < tmpTargetTime ) {
                    if ( tmpResult = super.awaitTermination( 1,
                                                             TimeUnit.NANOSECONDS ) ) {
                        break;
                    }
                    super.shutdownNow( );
                }
            }
            return tmpResult;
        }
    }

    public static class AsapScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

        protected AsapThreadPoolListener listener;

        public AsapScheduledThreadPoolExecutor( int coreThreads,
                                                int keepAlive,
                                                String namePrefix,
                                                AsapThreadPoolListener listener ) {
            this( coreThreads,
                  keepAlive,
                  new AsapThreadFactory( namePrefix,
                                         null ),
                  listener );
        }

        public AsapScheduledThreadPoolExecutor( int coreThreads,
                                                int keepAlive,
                                                ThreadFactory factory,
                                                AsapThreadPoolListener listener ) {
            super( coreThreads,
                   factory,
                   new ThreadPoolExecutor.AbortPolicy( ) );
            this.listener = listener;
            this.setKeepAliveTime( keepAlive,
                                   TimeUnit.MILLISECONDS );
            this.allowCoreThreadTimeOut( true );
            this.setRemoveOnCancelPolicy( true );
            this.prestartCoreThread( );
        }

        @Override
        protected void beforeExecute( Thread thread,
                                      Runnable runnable ) {
            super.beforeExecute( thread,
                                 runnable );
            if ( this.isShutdown( ) ) {
                if ( this.listener != null ) {
                    this.listener.abortingTask( thread,
                                                runnable );
                }
                thread.interrupt( );
            }
            else {
                if ( this.listener != null ) {
                    this.listener.beforeExecute( thread,
                                                 runnable );
                }
            }
        }

        @Override
        protected void afterExecute( Runnable runnable,
                                     Throwable throwabe ) {
            super.afterExecute( runnable,
                                throwabe );
            if ( this.listener != null ) {
                this.listener.afterExecute( runnable,
                                            throwabe );
            }
        }

        @Override
        protected void terminated( ) {
            super.terminated( );
            if ( this.listener != null ) {
                this.listener.terminated( );
            }
        }

        @Override
        public boolean awaitTermination( long timeout,
                                         TimeUnit unit )
            throws InterruptedException {
            boolean tmpResult = false;
            long tmpTimeoutMilis = TimeUnit.MILLISECONDS.convert( timeout,
                                                                  unit );
            long tmpTargetTime = ( System.currentTimeMillis( ) + tmpTimeoutMilis );
            while ( System.currentTimeMillis( ) < tmpTargetTime ) {
                if ( tmpResult = super.awaitTermination( 1,
                                                         TimeUnit.NANOSECONDS ) ) {
                    break;
                }
                super.shutdownNow( );
            }
            return tmpResult;
        }
    }

    public static void sleep( long timeValue ) {
        try {
            Thread.sleep( timeValue );
        }
        catch ( InterruptedException e ) {
        }
    }
}
