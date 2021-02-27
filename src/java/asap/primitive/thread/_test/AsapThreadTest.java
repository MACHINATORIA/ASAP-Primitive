package asap.primitive.thread._test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import asap.primitive.console.AbstractConsoleApplication;
import asap.primitive.dateTime.DateTimeHelper;
import asap.primitive.log.LogService.LogDetail;
import asap.primitive.log.LogService.LogLevel;
import asap.primitive.log.LogService.LogManager;
import asap.primitive.string.StringHelper;
import asap.primitive.thread.ThreadHelper;
import asap.primitive.thread.ThreadHelper.AsapFixedThreadPoolExecutor;
import asap.primitive.thread.ThreadHelper.AsapScheduledThreadPoolExecutor;
import asap.primitive.thread.ThreadHelper.AsapThreadPoolListener;

public class AsapThreadTest extends AbstractConsoleApplication {

    protected static int THREAD_FIXED_POOL_CORE_SIZE     = 5;

    protected static int THREAD_FIXED_POOL_MAX_SIZE      = 10;

    protected static int THREAD_FIXED_POOL_QUEUE_SIZE    = 5;

    protected static int THREAD_FIXED_POOL_TRY_COUNT     = 5;

    protected static int THREAD_FIXED_POOL_TRY_DELAY     = 5;

    protected static int THREAD_SCHEDULED_POOL_CORE_SIZE = 0;

    protected static int THREAD_POOL_KEEPALIVE           = 3000;

    protected static int THREAD_TEST_TASK_COUNT          = 20;

    protected static int THREAD_TEST_YIELD_COUNT         = 100;

    protected static int THREAD_TEST_YIELD_TIME          = 50;

    protected static int THREAD_TEST_RUN_DELAY           = 10000;

    protected static class TestTask implements Runnable {

        protected String executor;

        protected int    taskNum;

        protected int    sleepMilis;

        protected void logDebug( String format,
                                 Object... args ) {
            log.debug( "  %14s   %04d: %s",
                       Thread.currentThread( ).getName( ),
                       this.taskNum,
                       StringHelper.flawlessFormat( format,
                                                    args ) );
        }

        public TestTask( String executor,
                         int taskNum ) {
            this( executor,
                  taskNum,
                  0 );
        }

        public TestTask( String executor,
                         int taskNum,
                         int sleepMilis ) {
            this.executor = executor;
            this.taskNum = taskNum;
            this.sleepMilis = sleepMilis;
            this.logDebug( "criando/adicionando ao pool" );
        }

        @Override
        public void run( ) {
            Thread tmpThread = Thread.currentThread( );
            this.logDebug( "iniciando" );
            boolean tmpInterrupted = false;
            for ( int tmpPhaseCount = 1; tmpPhaseCount <= THREAD_TEST_YIELD_COUNT; tmpPhaseCount++ ) {
                if ( tmpThread.isInterrupted( ) ) {
                    tmpInterrupted = true;
                    this.logDebug( "interrompida antes da fase %d",
                                   tmpPhaseCount );
                    break;
                }
                this.logDebug( "executando fase #%d",
                               tmpPhaseCount );
                if ( this.sleepMilis > 0 ) {
                    try {
                        Thread.sleep( this.sleepMilis );
                    }
                    catch ( InterruptedException e ) {
                        tmpInterrupted = true;
                        this.logDebug( "interrompida depois da fase %d",
                                       tmpPhaseCount );
                        break;
                    }
                }
            }
            this.logDebug( "finalizando %s interrupção",
                           tmpInterrupted ? "com"
                                          : "sem" );
        }
    }

    protected static Integer taskNum = 1;

    protected void logDebug( String format,
                             Object... args ) {
        log.debug( "# %s",
                   StringHelper.flawlessFormat( format,
                                                args ) );
    }

    protected void runExecutorTest( ExecutorService executor )
        throws InterruptedException {
        String tmpExecutorName = executor.getClass( ).getSimpleName( );
        this.logDebug( "Iniciando teste do executor '%s'",
                       tmpExecutorName );
        for ( int tmpTaskCount = 0; tmpTaskCount < THREAD_TEST_TASK_COUNT; tmpTaskCount++ ) {
            // int tmpTaskNum;
            synchronized ( taskNum ) {
                // tmpTaskNum = taskNum;
                taskNum += 1;
            }
            TestTask tmpTask = new TestTask( tmpExecutorName,
                                             tmpTaskCount,
                                             THREAD_TEST_YIELD_TIME );
            if ( Thread.interrupted( ) ) {
                tmpTask.logDebug( "teste interropido (!!!!!)" );
                break;
            }
            tmpTask.logDebug( "adicionando ao executor '%s'",
                              tmpExecutorName );
            int tmpTryCount = 0;
            while ( true ) {
                try {
                    executor.execute( tmpTask );
                    break;
                }
                catch ( RejectedExecutionException e ) {
                    tmpTryCount += 1;
                    if ( tmpTryCount > THREAD_FIXED_POOL_TRY_COUNT ) {
                        tmpTask.logDebug( "abortando adição ao executor '%s'",
                                          tmpExecutorName );
                        break;
                    }
                    tmpTask.logDebug( "tentativa %d rejeitada pelo executor '%s' (!!!!!)",
                                      tmpTryCount,
                                      tmpExecutorName );
                    Thread.sleep( THREAD_FIXED_POOL_TRY_DELAY );
                }
            }
        }
        this.logDebug( "Finalizando teste do executor '%s'",
                       tmpExecutorName );
    }

    protected void stopExecutorTest( ExecutorService executor )
        throws InterruptedException {
        String tmpExecutorName = executor.getClass( ).getSimpleName( );
        this.logDebug( "Solicitando shutdown ao executor '%s'",
                       tmpExecutorName );
        executor.shutdownNow( );
        this.logDebug( "Shutdown solicitado ao executor '%s'",
                       tmpExecutorName );
        this.logDebug( "Aguardando término do executor '%s'",
                       tmpExecutorName );
        executor.awaitTermination( 2,
                                   TimeUnit.SECONDS );
        this.logDebug( "%s término do executor '%s'",
                       executor.isTerminated( ) ? "Concluído o"
                                                : "Falha no",
                       tmpExecutorName );
    }

    @SuppressWarnings( "unused" )
    protected void testThreadPoolExecutors( )
        throws InterruptedException {
        this.logDebug( "Criando executores" );
        List< ExecutorService > tmpExecutorServices = new ArrayList< ExecutorService >( );
        AsapThreadPoolListener tmpThreadPoolListener = new AsapThreadPoolListener( ) {

            @Override
            public void abortingTask( Thread thread,
                                      Runnable runnable ) {
                log.debug( "Abortando tarefa" );
            }

            @Override
            public void beforeExecute( Thread thread,
                                       Runnable runnable ) {
                log.debug( "Iniciando tarefa" );
            }

            @Override
            public void afterExecute( Runnable runnable,
                                      Throwable throwabe ) {
                log.debug( "Finalizando tarefa" );
            }

            @Override
            public void terminated( ) {
                log.debug( "Encerrando pool..." );
            }
        };
        if ( THREAD_FIXED_POOL_CORE_SIZE > 1 ) {
            tmpExecutorServices.add( new AsapFixedThreadPoolExecutor( THREAD_FIXED_POOL_CORE_SIZE,
                                                                      THREAD_FIXED_POOL_MAX_SIZE,
                                                                      THREAD_FIXED_POOL_QUEUE_SIZE,
                                                                      THREAD_POOL_KEEPALIVE,
                                                                      "Fixed",
                                                                      tmpThreadPoolListener ) );
        }
        if ( THREAD_SCHEDULED_POOL_CORE_SIZE > 1 ) {
            tmpExecutorServices.add( new AsapScheduledThreadPoolExecutor( THREAD_SCHEDULED_POOL_CORE_SIZE,
                                                                          THREAD_POOL_KEEPALIVE,
                                                                          "Scheduled",
                                                                          tmpThreadPoolListener ) );
        }
        this.logDebug( "Executores criados" );
        for ( ExecutorService tmpExecutor : tmpExecutorServices ) {
            this.logDebug( "Criando teste do executor '%s'",
                           tmpExecutor.getClass( ).getSimpleName( ) );
            tmpExecutor.execute( new Runnable( ) {

                @Override
                public void run( ) {
                    try {
                        AsapThreadTest.this.runExecutorTest( tmpExecutor );
                    }
                    catch ( InterruptedException e ) {
                        log.exception( e );
                    }
                }
            } );
        }
        ThreadHelper.sleep( THREAD_TEST_RUN_DELAY );
        if ( true ) {
            this.logDebug( "Solicitando shutdown aos executores" );
            for ( ExecutorService tmpExecutor : tmpExecutorServices ) {
                this.stopExecutorTest( tmpExecutor );
            }
        }
        else {
            this.logDebug( "Forçando shutdown dos executores" );
            while ( !Thread.interrupted( ) ) {
                boolean tmpRunning = false;
                for ( ExecutorService tmpExecutor : tmpExecutorServices ) {
                    if ( !tmpExecutor.isTerminated( ) ) {
                        tmpExecutor.shutdownNow( );
                        tmpRunning = true;
                        break;
                    }
                }
                if ( !tmpRunning ) {
                    break;
                }
            }
        }
        this.logDebug( "Concluído o término dos executores" );
    }

    @Override
    protected void _entry_point( String[ ] args )
        throws Throwable {
        LogManager.setMaxLevel( LogLevel.Trace );
        LogManager.setDetails( //LogDetail.CalendarDate,
                              LogDetail.TimeStamp,
                              //LogDetail.LevelName,
                              //LogDetail.ThreadInfo,
                              //LogDetail.SimpleClassName,
                              //LogDetail.MethodName,
                              //LogDetail.LineNumber //
                              null );
        log.info( "Iniciando teste de threads" );
        long tmpStartTime = System.currentTimeMillis( );
        try {
            this.testThreadPoolExecutors( );
            ThreadHelper.sleep( 10000 );
            this.testThreadPoolExecutors( );
        }
        finally {
            long tmpFinishTime = System.currentTimeMillis( );
            log.info( "Finalizando teste de threads ( %s decorridos )",
                      DateTimeHelper.formatElapsedTime( tmpFinishTime - tmpStartTime ) );
        }
    }

    public static void main( String[ ] args ) {
        AbstractConsoleApplication.execute( AsapThreadTest.class,
                                            args );
    }
}
