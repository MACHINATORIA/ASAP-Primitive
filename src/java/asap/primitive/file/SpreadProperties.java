package asap.primitive.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import asap.primitive.environment.EnvironmentHelper;
import asap.primitive.log.LogService.LogManager;
import asap.primitive.log.LogService.Logger;
import asap.primitive.process.ProcessHelper;
import asap.primitive.string.RegexHelper;
import asap.primitive.string.StringHelper;

@SuppressWarnings( "serial" )
public class SpreadProperties extends Properties {

    private static Logger    log = LogManager.getLogger( SpreadProperties.class );

    protected Class< ? >     contextClass;

    protected ClassLoader    classLoader;

    protected String         fileName;

    protected String         classPackageFilePath;

    protected List< String > jvmClasspathFilePaths;

    protected String         codeSourceFilePath;

    public SpreadProperties( Class< ? > contextClass )
        throws FileNotFoundException {
        this( contextClass,
              null,
              null );
    }

    public SpreadProperties( String fileName,
                             String fileExtension )
        throws FileNotFoundException {
        this( null,
              fileName,
              fileExtension );
    }

    public SpreadProperties( Class< ? > contextClass,
                             String fileName,
                             String fileExtension )
        throws FileNotFoundException {
        //
        if ( ( contextClass == null ) && ( fileName == null ) ) {
            throw new FileNotFoundException( "Impossível construir 'SpreadProperties' com 'contextClass' e 'fileName' nulos" );
        }
        this.contextClass = contextClass;
        this.classLoader = ( contextClass != null ) ? contextClass.getClassLoader( )
                                                    : Thread.currentThread( ).getContextClassLoader( );
        this.fileName = String.format( "%s.%s",
                                       ( ( fileName != null ) ? fileName
                                                              : contextClass.getSimpleName( ) ),
                                       ( ( fileExtension != null ) ? fileExtension
                                                                   : "properties" ) );
        //
        this.classPackageFilePath = ( contextClass == null ) ? null
                                                             : FileHelper.getResourcePath( this.contextClass,
                                                                                           this.fileName );
        //
        this.jvmClasspathFilePaths = new ArrayList< String >( );
        String[ ] tmpClassPaths = System.getProperty( "java.class.path" ).replace( "\"",
                                                                                   "" ).split( File.pathSeparator );
        for ( String tmpClassPath : tmpClassPaths ) {
            File tmpFile = new File( tmpClassPath );
            if ( tmpFile.isDirectory( ) ) {
                this.jvmClasspathFilePaths.add( String.format( "%s%s%s",
                                                               tmpClassPath,
                                                               File.separator,
                                                               fileName ) );
            }
        }
        //
        this.codeSourceFilePath = String.format( "%s%s%s",
                                                 EnvironmentHelper.codeSourcePath( ( contextClass != null ) ? contextClass
                                                                                                            : SpreadProperties.class ),
                                                 File.separator,
                                                 this.fileName );
    }

    protected boolean appendStream( Properties properties,
                                    InputStream stream )
        throws IOException {
        if ( stream == null ) {
            return false;
        }
        Properties tmpProperties = new Properties( );
        tmpProperties.load( stream );
        properties.putAll( tmpProperties );
        return true;
    }

    protected boolean appendFromClassLoader( Properties properties,
                                             String filePath )
        throws IOException {
        return this.appendStream( properties,
                                  this.classLoader.getResourceAsStream( filePath ) );
    }

    protected boolean appendFromFileSystem( Properties properties,
                                            String filePath )
        throws IOException {
        InputStream tmpStream = null;
        try {
            tmpStream = new FileInputStream( filePath );
        }
        catch ( FileNotFoundException e ) {
        }
        return this.appendStream( properties,
                                  tmpStream );
    }

    protected static Properties removeDuplicates( Properties targetProperties,
                                                  Properties sourceProperties ) {
        Properties tmpDuplicates = new Properties( );
        for ( String tmpTargetName : targetProperties.stringPropertyNames( ) ) {
            String tmpTargetValue = targetProperties.getProperty( tmpTargetName );
            String tmpSourceValue = sourceProperties.getProperty( tmpTargetName );
            if ( ( tmpSourceValue != null ) && ( tmpTargetValue.compareTo( tmpSourceValue ) == 0 ) ) {
                tmpDuplicates.put( tmpTargetName,
                                   tmpTargetValue );
                targetProperties.remove( tmpTargetName );
            }
        }
        return tmpDuplicates;
    }

    public static List< String > dumpProperties( Properties properties ) {
        List< String > tmpResult = new ArrayList< String >( );
        List< String > tmpNames = new ArrayList< String >( properties.stringPropertyNames( ) );
        Collections.sort( tmpNames );
        for ( String tmpName : tmpNames ) {
            tmpResult.add( String.format( "%s=%s",
                                          tmpName,
                                          properties.getProperty( tmpName ) ) );
        }
        //
        return tmpResult;
    }

    public SpreadProperties saveToFiles( )
        throws IOException {
        Properties tmpPropertiesToSave = new Properties( );
        tmpPropertiesToSave.putAll( this );
        //
        log.debug( "Salvando arquivos \"%s\"...",
                   this.fileName );
        /*
         * Elimina duplicidades com o arquivo no 'package' da classe
         */
        if ( this.classPackageFilePath != null ) {
            Properties tmpClassPackageProperties = new Properties( );
            if ( this.appendFromClassLoader( tmpClassPackageProperties,
                                             this.classPackageFilePath ) ) {
                Properties tmpClassPackageDifferences = removeDuplicates( tmpPropertiesToSave,
                                                                          tmpClassPackageProperties );
                log.debug( "Encontrado no 'package' da classe: \"%s\"%s",
                           this.classPackageFilePath,
                           ( tmpClassPackageDifferences.size( ) == 0 ) ? ""
                                                                       : String.format( "\nDiferenças:\n%s",
                                                                                        StringHelper.listToLines( dumpProperties( tmpClassPackageDifferences ) ) ) );
            }
        }
        /*
         * Elimina duplicidades com os arquivos encontrados nos diretórios do classpath da JVM 
         */
        for ( String tmpFileClassPath : this.jvmClasspathFilePaths ) {
            Properties tmpClasspathProperties = new Properties( );
            if ( this.appendFromFileSystem( tmpClasspathProperties,
                                            tmpFileClassPath ) ) {
                Properties tmpClasspathDifferences = removeDuplicates( tmpPropertiesToSave,
                                                                       tmpClasspathProperties );
                log.debug( "Encontrado no 'classpath' da JVM: \"%s\"%s",
                           tmpFileClassPath,
                           ( tmpClasspathDifferences.size( ) == 0 ) ? ""
                                                                    : String.format( "\nDiferenças:\n%s",
                                                                                     StringHelper.listToLines( dumpProperties( tmpClasspathDifferences ) ) ) );
            }
        }
        /*
         * Salva propriedades distintas na pasta da fonte de código
         */
        tmpPropertiesToSave.store( new FileOutputStream( new File( this.codeSourceFilePath ) ),
                                   "" );
        //
        return this;
    }

    public SpreadProperties loadFromFiles( )
        throws IOException {
        Properties tmpProperties = new Properties( );
        boolean tmpFileFound = false;
        log.debug( "Mesclando arquivos \"%s\"...",
                   this.fileName );
        /*
         * Carrega do 'package' da classe
         */
        if ( contextClass != null ) {
            if ( this.appendFromClassLoader( tmpProperties,
                                             this.classPackageFilePath ) ) {
                tmpFileFound = true;
                log.debug( "Encontrado no 'package' da classe: \"%s\"",
                           this.classPackageFilePath );
            }
        }
        /*
         * Carrega dos diretórios no 'classpath'
         */
        for ( String tmpFileClassPath : this.jvmClasspathFilePaths ) {
            if ( this.appendFromFileSystem( tmpProperties,
                                            tmpFileClassPath ) ) {
                tmpFileFound = true;
                log.debug( "Encontrado no 'classpath' da JVM: \"%s\"",
                           tmpFileClassPath );
                break;
            }
        }
        /*
         * Carrega da pasta da fonte de código
         */
        if ( this.appendFromFileSystem( tmpProperties,
                                        this.codeSourceFilePath ) ) {
            tmpFileFound = true;
            log.debug( "Encontrado na pasta 'codeSource': \"%s\"",
                       this.codeSourceFilePath );
        }
        if ( !tmpFileFound ) {
            throw new FileNotFoundException( String.format( "Nenhum arquivo \"%s\" encontrado.",
                                                            this.fileName ) );
        }
        this.putAll( tmpProperties );
        return this;
    }

    public static < T > void createFactories( Properties factoryConfigs,
                                              String registryName,
                                              Class< T > factorySuperClass,
                                              List< T > instanceList )
        throws Exception {
        String[ ] tmpFactoryConfigNames = SpreadProperties.filterKeyRegex( factoryConfigs,
                                                                           String.format( "\\A%s\\.%s\\d+\\z",
                                                                                          registryName,
                                                                                          factorySuperClass.getSimpleName( ) ) ).keySet( ).toArray( new String[ 0 ] );
        if ( tmpFactoryConfigNames.length == 0 ) {
            String tmpError = String.format( "Nenhuma configuração válida de fábrica \"%s\"",
                                             factorySuperClass.getSimpleName( ) );
            log.error( tmpError );
            throw new Exception( tmpError );
        }
        for ( int tmpIndex = 0; tmpIndex < tmpFactoryConfigNames.length; tmpIndex++ ) {
            String tmpFactoryConfigValue = factoryConfigs.getProperty( tmpFactoryConfigNames[ tmpIndex ] );
            if ( !tmpFactoryConfigValue.matches( ".+(.+)\\z" ) ) {
                String tmpError = String.format( "Configuração inválida de fábrica: \"%s=%s\"",
                                                 tmpFactoryConfigNames[ tmpIndex ],
                                                 tmpFactoryConfigValue );
                log.error( tmpError );
                throw new Exception( tmpError );
            }
            String[ ] tmpFactoryParams = tmpFactoryConfigValue.split( "[()]" );
            Class< ? > tmpFactoryClass;
            try {
                tmpFactoryClass = java.lang.Class.forName( tmpFactoryParams[ 0 ] );
            }
            catch ( Throwable e ) {
                String tmpError = ProcessHelper.briefExceptionMsg( "Erro ao carregar fábrica",
                                                                   e );
                log.error( tmpError );
                throw new Exception( tmpError );
            }
            if ( !factorySuperClass.isAssignableFrom( tmpFactoryClass ) ) {
                String tmpError = String.format( "Classe de fábrica \"%s\" não é subclasse de \"%s\"",
                                                 tmpFactoryParams[ 0 ],
                                                 factorySuperClass.getName( ) );
                log.error( tmpError );
                throw new Exception( tmpError );
            }
            log.debug( "Construindo objeto \"%s\" com parametros \"%s\"",
                       tmpFactoryParams[ 0 ],
                       tmpFactoryParams[ 1 ] );
            Object tmpFactoryInstance;
            try {
                Constructor< ? > tmpFactoryConstructor = tmpFactoryClass.getConstructor( new Class[ ] { String.class } );
                tmpFactoryInstance = tmpFactoryConstructor.newInstance( new Object[ ] { tmpFactoryParams[ 1 ].trim( ) } );
            }
            catch ( Throwable e ) {
                String tmpError = ProcessHelper.briefExceptionMsg( String.format( "Erro ao construir fábrica \"%s\" com parametros \"%s\"",
                                                                                  tmpFactoryParams[ 0 ],
                                                                                  tmpFactoryParams[ 1 ] ),
                                                                   e );
                log.error( tmpError );
                throw new Exception( tmpError );
            }
            instanceList.add( factorySuperClass.cast( tmpFactoryInstance ) );
        }
    }

    public static Properties filterKeyRegex( Properties properties,
                                             String regex ) {
        Properties tmpResult = new Properties( );
        Enumeration< Object > tmpKeys = properties.keys( );
        while ( tmpKeys.hasMoreElements( ) ) {
            String tmpKey = (String) tmpKeys.nextElement( );
            if ( tmpKey.matches( regex ) ) {
                tmpResult.put( tmpKey,
                               properties.get( tmpKey ) );
            }
        }
        return tmpResult;
    }

    public static Properties filterKeyPrefix( Properties properties,
                                              String prefix,
                                              boolean strip ) {
        Properties tmpResult = new Properties( );
        if ( prefix != null ) {
            String tmpPrefixRegex = RegexHelper.escapeRegexString( prefix );
            String tmpFilterRegex = String.format( "\\A%s\\..*",
                                                   tmpPrefixRegex );
            String tmpStripRegex = String.format( "\\A%s\\.",
                                                  tmpPrefixRegex );
            Enumeration< Object > tmpKeys = properties.keys( );
            while ( tmpKeys.hasMoreElements( ) ) {
                String tmpSourceKey = (String) tmpKeys.nextElement( );
                if ( tmpSourceKey.matches( tmpFilterRegex ) ) {
                    String tmpTargetKey = null;
                    if ( !strip ) {
                        tmpTargetKey = tmpSourceKey;
                    }
                    else {
                        tmpTargetKey = tmpSourceKey.replaceAll( tmpStripRegex,
                                                                "" );
                    }
                    tmpResult.put( tmpTargetKey,
                                   properties.get( tmpSourceKey ) );
                }
            }
        }
        else {
            tmpResult.putAll( properties );
        }
        return tmpResult;
    }
}
