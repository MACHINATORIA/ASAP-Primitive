package asap.primitive.environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;

import asap.primitive.string.IndentedStringBuilder;
import asap.primitive.string.StringHelper;

public class EnvironmentHelper {

    public static class ClassEnumerator {

        private Class< ? >                        domainClass;

        private Map< String, List< Class< ? > > > classesMap;

        IndentedStringBuilder                     enumerationReport;

        protected ClassEnumerator( Class< ? > domainClass ) {
            this.domainClass = domainClass;
            classesMap = new HashMap< String, List< Class< ? > > >( );
            enumerationReport = new IndentedStringBuilder( );
        }

        protected void buildFileSystemClassMap( String packageName,
                                                File packageDir ) {
            if ( packageDir.isDirectory( ) ) {
                enumerationReport.append( "Package: %s",
                                          ( ( packageName.compareTo( "" ) != 0 ) ? packageName
                                                                                 : "[default]" ) );
                enumerationReport.increaseIndent( );
                List< Class< ? > > tmpPackageClasses = new ArrayList< Class< ? > >( );
                for ( File tmpFile : packageDir.listFiles( ) ) {
                    if ( tmpFile.isDirectory( ) ) {
                        String tmpPackageName = String.format( "%s%s%s",
                                                               packageName,
                                                               ( ( packageName.compareTo( "" ) == 0 ) ? ""
                                                                                                      : "." ),
                                                               tmpFile.getName( ) );
                        buildFileSystemClassMap( tmpPackageName,
                                                 tmpFile );
                    }
                    else {
                        String tmpFileName = tmpFile.getName( );
                        if ( tmpFileName.matches( ".*\\.class\\z" ) && !tmpFileName.matches( ".*\\$\\d+.*\\z" ) ) {
                            String tmpClassName = String.format( "%s.%s",
                                                                 packageName,
                                                                 tmpFileName.replaceAll( "\\.class\\z",
                                                                                         "" ) );
                            try {
                                Class< ? > tmpClass = domainClass.getClassLoader( ).loadClass( tmpClassName );
                                tmpPackageClasses.add( tmpClass );
                                enumerationReport.append( "Classe: %s",
                                                          tmpClassName );
                            }
                            catch ( ClassNotFoundException e ) {
                                enumerationReport.append( "Falha carregando \"%s\"",
                                                          tmpClassName );
                            }
                        }
                    }
                }
                enumerationReport.decreaseIndent( );
                if ( tmpPackageClasses.size( ) > 0 ) {
                    classesMap.put( packageName,
                                    tmpPackageClasses );
                }
            }
        }

        protected void buildJarStreamClassMap( InputStream inputStream )
            throws IOException {
            JarInputStream tmpJarStream = new JarInputStream( inputStream );
            try {
                JarEntry tmpJarEntry;
                enumerationReport.increaseIndent( );
                List< String > tmpClassNames = new ArrayList< String >( );
                while ( ( tmpJarEntry = tmpJarStream.getNextJarEntry( ) ) != null ) {
                    if ( !tmpJarEntry.isDirectory( ) ) {
                        String tmpEntryName = tmpJarEntry.getName( );
                        if ( tmpEntryName.matches( ".*\\.class\\z" ) && !tmpEntryName.matches( ".*\\$\\d+.*\\z" ) ) {
                            tmpClassNames.add( tmpEntryName.replaceAll( "\\.class\\z",
                                                                        "" ).replaceAll( "/",
                                                                                         "." ) );
                        }
                    }
                }
                for ( String tmpClassName : tmpClassNames ) {
                    String tmpPackageName = tmpClassName.substring( 0,
                                                                    tmpClassName.lastIndexOf( "." ) );
                    try {
                        Class< ? > tmpClass = domainClass.getClassLoader( ).loadClass( tmpClassName );
                        List< Class< ? > > tmpPackageClassList = classesMap.get( tmpPackageName );
                        if ( tmpPackageClassList == null ) {
                            tmpPackageClassList = new ArrayList< Class< ? > >( );
                            classesMap.put( tmpPackageName,
                                            tmpPackageClassList );
                        }
                        tmpPackageClassList.add( tmpClass );
                        enumerationReport.append( "Classe: %s",
                                                  tmpClassName );
                    }
                    catch ( ClassNotFoundException e ) {
                        enumerationReport.append( "FALHA:  %s",
                                                  tmpClassName );
                        enumerationReport.appendIndented( "%s",
                                                          tmpClassName );
                    }
                }
                enumerationReport.decreaseIndent( );
            }
            finally {
                tmpJarStream.close( );
            }
        }

        protected void enumerateClassLoaderResources( )
            throws IOException {
            Enumeration< URL > tmpURLs = domainClass.getClassLoader( ).getResources( "." );
            while ( tmpURLs.hasMoreElements( ) ) {
                URL tmpURL = tmpURLs.nextElement( );
                enumerationReport.append( "URL: \"%s\"",
                                          tmpURL.toString( ) );
            }
        }

        protected void buildMap( ) {
            URL tmpCodeSourceURL = domainClass.getProtectionDomain( ).getCodeSource( ).getLocation( );
            String tmpCodeSource = tmpCodeSourceURL.toString( );
            enumerationReport.append( "Origem do código: \"%s\"",
                                      tmpCodeSource );
            if ( tmpCodeSource.endsWith( ".jar" ) ) {
                String tmpJarFileName = tmpCodeSource;
                try {
                    if ( tmpJarFileName.startsWith( "file:" ) ) {
                        tmpJarFileName = tmpJarFileName.replaceAll( "\\Afile:/",
                                                                    "" );
                        buildJarStreamClassMap( new FileInputStream( tmpJarFileName ) );
                    }
                    else if ( tmpJarFileName.startsWith( "jar:file:" ) ) {
                        tmpJarFileName = tmpJarFileName.replaceAll( "\\A.*!/",
                                                                    "" );
                        buildJarStreamClassMap( domainClass.getClassLoader( ).getResourceAsStream( tmpJarFileName ) );
                    }
                }
                catch ( IOException e ) {
                    enumerationReport.append( "Falha enumerando \"%s\" (\"%s\"):",
                                              tmpCodeSource,
                                              tmpJarFileName );
                    enumerationReport.appendIndented( StringHelper.escapeFormat( e.getMessage( ) ) );
                }
            }
            else {
                String tmpFileSystemPath = tmpCodeSource.replaceAll( "\\Afile:/",
                                                                     "" ).replaceAll( "%20",
                                                                                      " " );
                buildFileSystemClassMap( "",
                                         new File( tmpFileSystemPath ) );
            }
        }

        //        public static OutputStream getCodeRootFileSystemOutputStream( Class< ? > applicationDomainClass,
        //                                                                      String fileName ) {
        //            OutputStream tmpResult = null;
        //            URL tmpCodeSourceURL = applicationDomainClass.getProtectionDomain( ).getCodeSource( ).getLocation( );
        //            String tmpCodeSource = tmpCodeSourceURL.toString( );
        //            String tmpAbsoluteRootFilePath = null;
        //            if ( tmpCodeSource.endsWith( ".jar" ) ) {
        //                String tmpJarFileName = tmpCodeSource;
        //                if ( tmpJarFileName.startsWith( "file:" ) ) {
        //                    String tmpFileSystemJarFileName = tmpJarFileName.replaceAll( "\\Afile:/",
        //                                                                                 "" );
        //                    File tmpFileSystemJarFile = new File( tmpFileSystemJarFileName );
        //                    String tmpTargetFileRelativeName = fileName.replaceAll( "\\\\",
        //                                                                            "/" );
        //                    tmpAbsoluteRootFilePath = tmpFileSystemJarFile.getAbsolutePath( );
        ////                    String tmpTargetFilePath = String.format( "%s%s%s",
        ////                                                              tmpFileSystemJarFile.getAbsolutePath( ),
        ////                                                              File.separator,
        ////                                                              tmpTargetFileRelativeName );
        ////                    tmpResult = new FileInputStream( tmpTargetFilePath );
        //                }
        //                else if ( tmpJarFileName.startsWith( "jar:file:" ) ) {
        //                    tmpAbsoluteRootFilePath = tmpJarFileName.replaceAll( "\\A.*!/",
        //                                                                         "" );
        //                    String tmpInnerJarFileName = tmpJarFileName.replaceAll( "\\A.*!/",
        //                                                                            "" );
        //                    String tmpTargetFileRelativeName = fileName.replaceAll( "",
        //                                                                            "." );
        //                    tmpResult = applicationDomainClass.getClassLoader( ).getResourceAsStream( String.format( "%s.%s",
        //                                                                                                             tmpInnerJarFileName,
        //                                                                                                             tmpTargetFileRelativeName ) );
        //                }
        //            }
        //            else {
        //                String tmpFileSystemPath = tmpCodeSource.replaceAll( "\\Afile:/",
        //                                                                     "" ).replaceAll( "%20",
        //                                                                                      " " );
        //                String tmpTargetFileRelativeName = fileName.replaceAll( "\\\\",
        //                                                                        "/" );
        //                String tmpTargetFilePath = String.format( "%s%s%s",
        //                                                          tmpFileSystemPath,
        //                                                          File.separator,
        //                                                          tmpTargetFileRelativeName );
        //                tmpResult = new FileInputStream( tmpTargetFilePath );
        //            }
        //            return tmpResult;
        //        }
        public static InputStream getCodeRootFileInputStream( Class< ? > applicationDomainClass,
                                                              String fileName )
            throws FileNotFoundException {
            InputStream tmpResult = null;
            URL tmpCodeSourceURL = applicationDomainClass.getProtectionDomain( ).getCodeSource( ).getLocation( );
            String tmpCodeSource = tmpCodeSourceURL.toString( );
            if ( tmpCodeSource.endsWith( ".jar" ) ) {
                String tmpJarFileName = tmpCodeSource;
                String tmpTargetFilePath = tmpJarFileName;
                if ( tmpJarFileName.startsWith( "file:" ) ) {
                    String tmpFileSystemJarFileName = tmpJarFileName.replaceAll( "\\Afile:/",
                                                                                 "" );
                    File tmpFileSystemJarFile = new File( tmpFileSystemJarFileName );
                    String tmpTargetFileRelativeName = fileName.replaceAll( "\\\\",
                                                                            "/" );
                    tmpTargetFilePath = String.format( "%s%s%s",
                                                       tmpFileSystemJarFile.getAbsolutePath( ),
                                                       File.separator,
                                                       tmpTargetFileRelativeName );
                    tmpResult = new FileInputStream( tmpTargetFilePath );
                }
                else if ( tmpJarFileName.startsWith( "jar:file:" ) ) {
                    String tmpInnerJarFilePath = tmpJarFileName.replaceAll( "\\Ajar:file:/",
                                                                            "" ).replaceAll( ".*!/\\z",
                                                                                             "" );
                    tmpResult = applicationDomainClass.getClassLoader( ).getResourceAsStream( String.format( "%s.%s",
                                                                                                             tmpInnerJarFilePath,
                                                                                                             tmpJarFileName ) );
                }
            }
            else {
                String tmpFileSystemFolder = tmpCodeSource.replaceAll( "\\Afile:/",
                                                                       "" );
                tmpFileSystemFolder = tmpFileSystemFolder.replaceAll( "%20",
                                                                      " " );
                tmpFileSystemFolder = tmpFileSystemFolder.replaceAll( "([\\/]bin)?[\\/]\\z",
                                                                      "" );
                tmpFileSystemFolder = tmpFileSystemFolder.replaceAll( "/",
                                                                      Matcher.quoteReplacement( File.separator ) );
                String tmpTargetFileRelativeName = fileName.replaceAll( "\\A[\\/]",
                                                                        "" );
                tmpTargetFileRelativeName = tmpTargetFileRelativeName.replaceAll( "/",
                                                                                  Matcher.quoteReplacement( File.separator ) );
                String tmpTargetFilePath = String.format( "%s%s%s",
                                                          tmpFileSystemFolder,
                                                          File.separator,
                                                          tmpTargetFileRelativeName );
                tmpResult = new FileInputStream( tmpTargetFilePath );
            }
            return tmpResult;
        }

        public static String buildClassMap( Map< String, List< Class< ? > > > map,
                                            Class< ? > domainClass ) {
            ClassEnumerator tmpClassEnumerator = new ClassEnumerator( domainClass );
            tmpClassEnumerator.buildMap( );
            if ( map != null ) {
                map.putAll( tmpClassEnumerator.classesMap );
            }
            return tmpClassEnumerator.enumerationReport.getResult( );
        }
    }

    public static class ObjectDissect {

        protected static void appendValue( String caption,
                                           IndentedStringBuilder result,
                                           String description ) {
            result.append( caption );
            result.increaseIndent( );
            result.append( description );
            result.decreaseIndent( );
        }

        protected static void listAnnotationNames( String caption,
                                                   IndentedStringBuilder result,
                                                   Annotation[ ] annotations ) {
            if ( annotations.length > 0 ) {
                result.append( caption );
                result.increaseIndent( );
                for ( Annotation tmpAnnotation : annotations ) {
                    result.append( tmpAnnotation.toString( ) );
                }
                result.decreaseIndent( );
            }
        }

        protected static void listClassNames( String caption,
                                              IndentedStringBuilder result,
                                              Class< ? >[ ] classes ) {
            if ( classes.length > 0 ) {
                result.append( caption );
                result.increaseIndent( );
                for ( Class< ? > tmpSuperClass : classes ) {
                    result.append( tmpSuperClass.getName( ) );
                }
                result.decreaseIndent( );
            }
        }

        protected static void describeConstructor( IndentedStringBuilder result,
                                                   Constructor< ? > constructor ) {
            StringBuilder tmpParameters = new StringBuilder( );
            for ( Class< ? > tmpSuperClass : constructor.getParameterTypes( ) ) {
                tmpParameters.append( tmpSuperClass.getName( ) );
            }
            result.append( "%s(%s)",
                           constructor.getDeclaringClass( ).getSimpleName( ),
                           tmpParameters.toString( ) );
            result.increaseIndent( );
            if ( constructor.isSynthetic( ) ) {
                result.append( "Sintético" );
            }
            listAnnotationNames( "Anotações:",
                                 result,
                                 constructor.getAnnotations( ) );
            appendValue( "Modificadores:",
                         result,
                         Modifier.toString( constructor.getModifiers( ) ) );
            listClassNames( ( "Parâmetros"
                              + ( constructor.isVarArgs( ) ? " (vararg)"
                                                           : "" )
                              + ":" ),
                            result,
                            constructor.getParameterTypes( ) );
            listClassNames( "Exceções:",
                            result,
                            constructor.getExceptionTypes( ) );
            appendValue( "Auto-descrição:",
                         result,
                         constructor.toGenericString( ) );
            result.decreaseIndent( );
        }

        protected static void describeField( IndentedStringBuilder result,
                                             Field field ) {
            result.append( field.getName( ) );
            result.increaseIndent( );
            if ( field.isSynthetic( ) ) {
                result.append( "Sintético" );
            }
            listAnnotationNames( "Anotações:",
                                 result,
                                 field.getAnnotations( ) );
            appendValue( "Modificadores:",
                         result,
                         Modifier.toString( field.getModifiers( ) ) );
            appendValue( "Tipo:",
                         result,
                         field.getType( ).getSimpleName( ) );
            appendValue( "Auto-descrição:",
                         result,
                         field.toGenericString( ) );
            result.decreaseIndent( );
        }

        protected static void describeMethod( IndentedStringBuilder result,
                                              Method method ) {
            result.append( method.getName( ) );
            result.increaseIndent( );
            if ( method.isSynthetic( ) ) {
                result.append( "Sintético" );
            }
            else {
                appendValue( "Declarado em:",
                             result,
                             method.getDeclaringClass( ).getName( ) );
            }
            listAnnotationNames( "Anotações:",
                                 result,
                                 getInheritedMethodAnnotations( method ) );
            appendValue( "Modificadores:",
                         result,
                         Modifier.toString( method.getModifiers( ) ) );
            appendValue( "Retorno:",
                         result,
                         method.getReturnType( ).getName( ) );
            listClassNames( ( "Parâmetros"
                              + ( method.isVarArgs( ) ? " (vararg)"
                                                      : "" )
                              + ":" ),
                            result,
                            method.getParameterTypes( ) );
            listClassNames( "Exceções:",
                            result,
                            method.getExceptionTypes( ) );
            appendValue( "Auto-descrição:",
                         result,
                         method.toGenericString( ) );
            result.decreaseIndent( );
        }

        protected static void describeClass( IndentedStringBuilder result,
                                             Class< ? > classObject ) {
            result.append( classObject.getName( ) );
            result.increaseIndent( );
            if ( classObject.isAnonymousClass( ) ) {
                result.append( "Anônima" );
            }
            else if ( classObject.isLocalClass( ) ) {
                result.append( "Classe local" );
            }
            else if ( classObject.isMemberClass( ) ) {
                result.append( "Classe membro" );
            }
            else if ( classObject.isSynthetic( ) ) {
                result.append( "Sintética" );
            }
            listAnnotationNames( "Anotações:",
                                 result,
                                 classObject.getAnnotations( ) );
            Class< ? > tmpSuperClass = classObject.getSuperclass( );
            if ( tmpSuperClass != null ) {
                appendValue( "Superclasse:",
                             result,
                             tmpSuperClass.getName( ) );
            }
            listClassNames( "Interfaces:",
                            result,
                            classObject.getInterfaces( ) );
            Constructor< ? >[ ] tmpClassConstructors = classObject.getConstructors( );
            if ( tmpClassConstructors.length > 0 ) {
                result.append( "Construtores:" );
                result.increaseIndent( );
                for ( Constructor< ? > tmpConstructor : tmpClassConstructors ) {
                    describeConstructor( result,
                                         tmpConstructor );
                }
                result.decreaseIndent( );
            }
            Field[ ] tmpClassFields = classObject.getFields( );
            if ( tmpClassFields.length > 0 ) {
                result.append( "Propriedades:" );
                result.increaseIndent( );
                for ( Field tmpField : tmpClassFields ) {
                    describeField( result,
                                   tmpField );
                }
                result.decreaseIndent( );
            }
            Method[ ] tmpClassMethods = classObject.getMethods( );
            if ( tmpClassMethods.length > 0 ) {
                result.append( "Métodos:\n" );
                result.increaseIndent( );
                for ( Method tmpMethod : tmpClassMethods ) {
                    describeMethod( result,
                                    tmpMethod );
                }
                result.decreaseIndent( );
            }
            result.decreaseIndent( );
        }

        public static Annotation[ ] getInheritedMethodAnnotations( Method method ) {
            List< Annotation > tmpAnnotations = new ArrayList< Annotation >( );
            tmpAnnotations.addAll( new ArrayList< Annotation >( Arrays.asList( method.getAnnotations( ) ) ) );
            List< Class< ? > > tmpClasses = new ArrayList< Class< ? > >( );
            Class< ? > tmpDeclaringClass = method.getDeclaringClass( );
            tmpClasses.addAll( Arrays.asList( tmpDeclaringClass.getInterfaces( ) ) );
            Class< ? > tmpSuperClass = tmpDeclaringClass.getSuperclass( );
            if ( ( tmpSuperClass != null ) && !tmpSuperClass.getPackage( ).getName( ).matches( "\\Ajava\\..*" ) ) {
                tmpClasses.add( tmpSuperClass );
            }
            for ( Class< ? > tmpClass : tmpClasses ) {
                Method tmpSuperMethod;
                try {
                    tmpSuperMethod = tmpClass.getMethod( method.getName( ),
                                                         method.getParameterTypes( ) );
                    if ( tmpSuperMethod != null ) {
                        tmpAnnotations.addAll( new ArrayList< Annotation >( Arrays.asList( getInheritedMethodAnnotations( tmpSuperMethod ) ) ) );
                    }
                }
                catch ( Throwable e ) {
                }
            }
            return tmpAnnotations.toArray( new Annotation[ 0 ] );
        }

        public static String describe( Object object ) {
            IndentedStringBuilder tmpResult = new IndentedStringBuilder( );
            Class< ? > tmpClass = object.getClass( );
            if ( tmpClass.isAnnotation( ) ) {
                tmpResult.append( "Anotação" );
            }
            else if ( tmpClass.isArray( ) ) {
                tmpResult.append( "Array" );
            }
            else if ( tmpClass.isEnum( ) ) {
                tmpResult.append( "Enumerado" );
            }
            else if ( tmpClass.isPrimitive( ) ) {
                tmpResult.append( "Primitivo" );
            }
            else if ( tmpClass.isInterface( ) ) {
                tmpResult.append( "Interface" );
                describeClass( tmpResult,
                               tmpClass );
            }
            else {
                describeClass( tmpResult,
                               tmpClass );
            }
            return tmpResult.getResult( );
        }
    }

    public static class ResourceFinder {

        public static InputStream openWithClassLoader( Class< ? > contextClass,
                                                       String resourcePath ) {
            ClassLoader tmpClassLoader = null;
            if ( contextClass != null ) {
                tmpClassLoader = contextClass.getClassLoader( );
            }
            else {
                tmpClassLoader = Thread.currentThread( ).getContextClassLoader( );
            }
            return tmpClassLoader.getResourceAsStream( resourcePath );
        }

        public static InputStream openWithFileSystem( String resourceName ) {
            InputStream tmpResult = null;
            try {
                tmpResult = new FileInputStream( resourceName );
            }
            catch ( FileNotFoundException e ) {
            }
            return tmpResult;
        }

        public static String pathFromClassPackage( Class< ? > contextClass,
                                                   String resourceName ) {
            return String.format( "%s/%s",
                                  contextClass.getPackage( ).getName( ).replace( ".",
                                                                                 "/" ),
                                  resourceName );
        }

        public static String pathFromCurrentDir( String resourceName ) {
            return String.format( ".%s%s",
                                  File.separator,
                                  resourceName );
        }

        public static InputStream openFromCodeSource( Class< ? > clazz,
                                                      String resourceName ) {
            InputStream tmpResult = null;
            URL tmpCodeSourceURL = clazz.getProtectionDomain( ).getCodeSource( ).getLocation( );
            String tmpCodeSource = tmpCodeSourceURL.toString( );
            if ( tmpCodeSource.endsWith( ".jar" ) ) {
                String tmpJarFileName = tmpCodeSource;
                String tmpTargetFilePath = tmpJarFileName;
                if ( tmpJarFileName.startsWith( "file:" ) ) {
                    String tmpFileSystemJarFileName = tmpJarFileName.replaceAll( "\\Afile:/",
                                                                                 "" );
                    File tmpFileSystemJarFile = new File( tmpFileSystemJarFileName );
                    String tmpTargetFileRelativeName = resourceName.replaceAll( "\\\\",
                                                                                "/" );
                    tmpTargetFilePath = String.format( "%s%s%s",
                                                       tmpFileSystemJarFile.getAbsolutePath( ),
                                                       File.separator,
                                                       tmpTargetFileRelativeName );
                    tmpResult = openWithFileSystem( tmpTargetFilePath );
                }
                else if ( tmpJarFileName.startsWith( "jar:file:" ) ) {
                    String tmpInnerJarFilePath = tmpJarFileName.replaceAll( "\\Ajar:file:/",
                                                                            "" ).replaceAll( ".*!/\\z",
                                                                                             "" );
                    tmpResult = openWithClassLoader( clazz,
                                                     String.format( "%s.%s",
                                                                    tmpInnerJarFilePath,
                                                                    tmpJarFileName ) );
                }
            }
            else {
                String tmpFileSystemFolder = tmpCodeSource.replaceAll( "\\Afile:/",
                                                                       "" );
                tmpFileSystemFolder = tmpFileSystemFolder.replaceAll( "%20",
                                                                      " " );
                tmpFileSystemFolder = tmpFileSystemFolder.replaceAll( "([\\/]bin)?[\\/]\\z",
                                                                      "" );
                tmpFileSystemFolder = tmpFileSystemFolder.replaceAll( "/",
                                                                      Matcher.quoteReplacement( File.separator ) );
                String tmpTargetFileRelativeName = resourceName.replaceAll( "\\A[\\/]",
                                                                            "" );
                tmpTargetFileRelativeName = tmpTargetFileRelativeName.replaceAll( "/",
                                                                                  Matcher.quoteReplacement( File.separator ) );
                String tmpTargetFilePath = String.format( "%s%s%s",
                                                          tmpFileSystemFolder,
                                                          File.separator,
                                                          tmpTargetFileRelativeName );
                tmpResult = openWithFileSystem( tmpTargetFilePath );
            }
            return tmpResult;
        }

        public static InputStream openFromClassPaths( String resourceName ) {
            return null;
        }

        public static InputStream openFromSystemPaths( String resourceName ) {
            return null;
        }
    }

    public static String codeSourcePath( Class< ? > clazz ) {
        String tmpResult = ".";
        URL tmpCodeSourceURL = clazz.getProtectionDomain( ).getCodeSource( ).getLocation( );
        String tmpCodeSource = tmpCodeSourceURL.toString( );
        if ( tmpCodeSource.endsWith( ".jar" ) ) {
            String tmpJarFileName = tmpCodeSource;
            String tmpFileSystemJarFileName = ".";
            if ( tmpJarFileName.startsWith( "file:" ) ) {
                tmpFileSystemJarFileName = tmpJarFileName.replaceAll( "\\Afile:/",
                                                                      "" );
            }
            else if ( tmpJarFileName.startsWith( "jar:file:" ) ) {
                tmpFileSystemJarFileName = tmpJarFileName.replaceAll( "\\Ajar:file:/",
                                                                      "" ).replaceAll( "!/.*\\z",
                                                                                       "" );
            }
            File tmpFileSystemJarFile = new File( tmpFileSystemJarFileName );
            tmpResult = tmpFileSystemJarFile.getParentFile( ).getAbsolutePath( );
        }
        else {
            String tmpFileSystemFolder = tmpCodeSource.replaceAll( "\\Afile:/",
                                                                   "" );
            tmpFileSystemFolder = tmpFileSystemFolder.replaceAll( "%20",
                                                                  " " );
            tmpFileSystemFolder = tmpFileSystemFolder.replaceAll( "([\\/]bin)?[\\/]\\z",
                                                                  "" );
            tmpResult = tmpFileSystemFolder.replaceAll( "/",
                                                        Matcher.quoteReplacement( File.separator ) );
        }
        return tmpResult;
    }

    public static boolean isWindowsPlatform( ) {
        return ( System.getProperty( "os.name" ).contains( "Windows" ) );
    }

    public static boolean isWindows64( ) {
        return ( System.getenv( "ProgramFiles(x86)" ) != null );
    }

    public static boolean isJRE64( ) {
        return ( System.getProperty( "os.arch" ).indexOf( "64" ) != -1 );
    }
}
