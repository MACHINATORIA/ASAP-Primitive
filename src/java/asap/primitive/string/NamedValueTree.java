package asap.primitive.string;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import asap.primitive.console.AbstractConsoleApplication;

@XmlRootElement( name = "namedValueTree",
                 namespace = "##default" )
@XmlType( name = "namedValueTree",
          propOrder = { "name",
                        "value",
                        "branches" } )
@XmlAccessorType( XmlAccessType.NONE )
public class NamedValueTree {

    public static NamedValueTree create( String name,
                                         String value ) {
        return new NamedValueTree( name,
                                   value );
    }

    public static NamedValueTree create( String name,
                                         Boolean value ) {
        return new NamedValueTree( name,
                                   ( value != null ) ? value.toString( )
                                                     : null );
    }

    public static NamedValueTree create( String name,
                                         Long value ) {
        return new NamedValueTree( name,
                                   ( value != null ) ? value.toString( )
                                                     : null );
    }

    public static NamedValueTree create( String name ) {
        return NamedValueTree.create( name,
                                      (String) null );
    }

    @XmlAttribute( required = true )
    protected String                 name;

    @XmlAttribute( required = false )
    protected String                 value;

    @XmlElement( required = true,
                 name = "branch" )
    protected List< NamedValueTree > branches;

    protected NamedValueTree( String name,
                              String value ) {
        this.name = name;
        this.value = value;
        this.branches = new ArrayList< NamedValueTree >( );
    }

    protected NamedValueTree( ) {
        this( "",
              null );
    }

    public String name( ) {
        return this.getName( );
    }

    public String get( String defaultValue ) {
        String tmpValue = this.getValue( );
        return ( tmpValue != null ) ? tmpValue
                                    : defaultValue;
    }

    public String get( ) {
        return this.get( null );
    }

    public Boolean getBoolean( Boolean defaultValue ) {
        String tmpValue = this.getValue( );
        return ( tmpValue != null ) ? Boolean.parseBoolean( tmpValue )
                                    : defaultValue;
    }

    public Boolean getBoolean( ) {
        return this.getBoolean( null );
    }

    public Integer getInteger( Integer defaultValue ) {
        String tmpValue = this.getValue( );
        return ( tmpValue != null ) ? (Integer) Integer.parseInt( tmpValue )
                                    : defaultValue;
    }

    public Integer getInteger( ) {
        return this.getInteger( null );
    }

    public Long getLong( Long defaultValue ) {
        String tmpValue = this.getValue( );
        return ( tmpValue != null ) ? Long.parseLong( tmpValue )
                                    : defaultValue;
    }

    public Long getLong( ) {
        return this.getLong( null );
    }

    public NamedValueTree set( String value ) {
        this.setValue( value );
        return this;
    }

    public NamedValueTree set( Boolean value ) {
        this.setValue( ( value != null ) ? value.toString( )
                                         : null );
        return this;
    }

    public NamedValueTree set( Integer value ) {
        this.setValue( ( value != null ) ? value.toString( )
                                         : null );
        return this;
    }

    public NamedValueTree set( Long value ) {
        this.setValue( ( value != null ) ? value.toString( )
                                         : null );
        return this;
    }

    public NamedValueTree add( NamedValueTree... branches ) {
        for ( NamedValueTree tmpBranch : branches ) {
            if ( tmpBranch != null ) {
                this.branches.add( tmpBranch );
            }
        }
        return this;
    }

    public NamedValueTree add( String name,
                               String value ) {
        NamedValueTree tmpResult = NamedValueTree.create( name,
                                                          value );
        this.add( tmpResult );
        return tmpResult;
    }

    public NamedValueTree add( String name,
                               Boolean value ) {
        NamedValueTree tmpResult = NamedValueTree.create( name,
                                                          value );
        this.add( tmpResult );
        return tmpResult;
    }

    public NamedValueTree add( String name,
                               Long value ) {
        NamedValueTree tmpResult = NamedValueTree.create( name,
                                                          value );
        this.add( tmpResult );
        return tmpResult;
    }

    public NamedValueTree add( String name ) {
        return this.add( name,
                         (String) null );
    }

    public List< NamedValueTree > branches( ) {
        return this.branches;
    }

    public NamedValueTree branch( String branchPath,
                                  String defaultValue ) {
        int tmpDotIndex = branchPath.indexOf( "." );
        String tmpBranchName = ( tmpDotIndex < 0 ) ? branchPath
                                                   : branchPath.substring( 0,
                                                                           tmpDotIndex );
        NamedValueTree tmpBranch = null;
        for ( NamedValueTree tmpBranchSearch : this.branches ) {
            if ( tmpBranchSearch.name.compareTo( tmpBranchName ) == 0 ) {
                tmpBranch = tmpBranchSearch;
                break;
            }
        }
        if ( tmpBranch == null ) {
            tmpBranch = new NamedValueTree( tmpBranchName,
                                            ( tmpDotIndex < 0 ) ? defaultValue
                                                                : null );
            this.branches.add( tmpBranch );
        }
        NamedValueTree tmpResult = ( tmpDotIndex < 0 ) ? tmpBranch
                                                       : tmpBranch.branch( branchPath.substring( tmpDotIndex + 1 ),
                                                                           defaultValue );
        return tmpResult;
    }

    public NamedValueTree branch( String branchPath,
                                  Boolean defaultValue ) {
        return this.branch( branchPath,
                            ( defaultValue != null ) ? defaultValue.toString( )
                                                     : null );
    }

    public NamedValueTree branch( String branchPath,
                                  Long defaultValue ) {
        return this.branch( branchPath,
                            ( defaultValue != null ) ? defaultValue.toString( )
                                                     : null );
    }

    public NamedValueTree branch( String branchPath ) {
        return this.branch( branchPath,
                            (String) null );
    }

    public NamedValueTree search( String branchPath ) {
        NamedValueTree tmpResult = null;
        if ( this.branches != null ) {
            int tmpDotIndex = branchPath.indexOf( "." );
            String tmpBranchName = ( tmpDotIndex < 0 ) ? branchPath
                                                       : branchPath.substring( 0,
                                                                               tmpDotIndex );
            for ( NamedValueTree tmpBranch : this.branches ) {
                if ( tmpBranch.name.compareTo( tmpBranchName ) == 0 ) {
                    tmpResult = ( tmpDotIndex < 0 ) ? tmpBranch
                                                    : tmpBranch.search( branchPath.substring( tmpDotIndex + 1 ) );
                    break;
                }
            }
        }
        return tmpResult;
    }

    public String toXml( )
        throws JAXBException {
        //
        JAXBContext tmpJaxbContext = JAXBContext.newInstance( NamedValueTree.class );
        Marshaller tmpMarshaller = tmpJaxbContext.createMarshaller( );
        tmpMarshaller.setProperty( "jaxb.encoding",
                                   System.getProperty( "file.encoding" ) );
        ByteArrayOutputStream tmpBytes = new ByteArrayOutputStream( );
        tmpMarshaller.marshal( this,
                               tmpBytes );
        String tmpResult = new String( tmpBytes.toByteArray( ) );
        //
        return tmpResult;
    }

    public static NamedValueTree fromXml( String xmlString )
        throws JAXBException {
        //
        JAXBContext tmpJaxbContext = JAXBContext.newInstance( NamedValueTree.class );
        Unmarshaller tmpUnmarshaller = tmpJaxbContext.createUnmarshaller( );
        ByteArrayInputStream tmpInputStream = new ByteArrayInputStream( xmlString.getBytes( ) );
        NamedValueTree tmpResult = (NamedValueTree) tmpUnmarshaller.unmarshal( tmpInputStream );
        //
        return tmpResult;
    }

    protected String getName( ) {
        return this.name;
    }

    protected void setName( String name ) {
        this.name = name;
    }

    protected String getValue( ) {
        return this.value;
    }

    protected void setValue( String value ) {
        this.value = value;
    }

    protected List< NamedValueTree > getBranches( ) {
        return this.branches;
    }

    protected NamedValueTree setBranches( List< NamedValueTree > branches ) {
        this.branches = new ArrayList< NamedValueTree >( branches );
        return this;
    }

    public static class Test extends AbstractConsoleApplication {

        protected NamedValueTree createStatic( ) {
            NamedValueTree tmpCreateNull = //
                            NamedValueTree.create( "create-null" );
            //
            NamedValueTree tmpCreateValue = //
                            NamedValueTree.create( "create-value",
                                                   "createValue" );
            //
            NamedValueTree tmpCreateSet = //
                            NamedValueTree.create( "create-set" )//
                                            .set( "createSet" );
            //
            NamedValueTree tmpCreateAddMany = //
                            NamedValueTree.create( "create-null-addMany" )//
                                            .add( tmpCreateNull,
                                                  tmpCreateValue,
                                                  tmpCreateSet );
            //
            NamedValueTree tmpCreateAddOne = //
                            NamedValueTree.create( "create-value-addOne",
                                                   "createAddOne" )//
                                            .add( "addOne-Set",
                                                  "addOneSet" );
            //
            NamedValueTree tmpCreateBranch = //
                            NamedValueTree.create( "create-set-branch" ) //
                                            .set( "createSetBranch" )//
                                            .branch( "branch-value-1.branch-value-2",
                                                     "branchValue-2" ) //
                                            .branch( "branch-set-1.branch-set-2" )//
                                            .set( "branchSet-2" );
            //
            NamedValueTree tmpConfig = NamedValueTree.create( "test-file",
                                                              "testFile" ).add( tmpCreateAddMany,
                                                                                tmpCreateAddOne,
                                                                                tmpCreateBranch );
            tmpConfig.branch( "branch/.branch\\.branch|",
                              "bars" );
            tmpConfig.branch( "branch%%.branch&.branch*",
                              "signs" );
            tmpConfig.branch( "branch-.branch_.branch+.branch=",
                              "operations" );
            tmpConfig.branch( "branch .branch,.branch;.branch:",
                              "separators" );
            tmpConfig.branch( "branch~.branch^.branch´.branch`",
                              "accents" );
            tmpConfig.branch( "branch'.branch@.branch#.branch$",
                              "tags" );
            tmpConfig.branch( "branch!.branch?.branch''.branch\"\"",
                              "specials" );
            tmpConfig.branch( "branch<>.branch().branch[].branch{}",
                              "groups" );
            //
            return tmpConfig;
        }

        protected String describeConfig( NamedValueTree config,
                                         String description,
                                         String path ) {
            NamedValueTree tmpItem = config.search( path );
            return String.format( "%15s: %20s -> %s",
                                  description,
                                  path,
                                  ( tmpItem == null ) ? "null-item"
                                                      : StringHelper.nullDefault( "null-value",
                                                                                  tmpItem.getValue( ) ) );
        }

        protected void dumpConfigTree_recursive( IndentedStringBuilder result,
                                                 NamedValueTree config ) {
            boolean tmpHasSubItems = ( config.branches( ).size( ) > 0 );
            result.append( "%s: %s%s",
                           config.getName( ),
                           StringHelper.nullText( config.getValue( ) ),
                           tmpHasSubItems ? " {"
                                          : ";" );
            if ( tmpHasSubItems ) {
                result.increaseIndent( );
                for ( NamedValueTree tmpConfig : config.branches ) {
                    this.dumpConfigTree_recursive( result,
                                                   tmpConfig );
                }
                result.decreaseIndent( );
                result.append( "}" );
            }
        }

        protected String dumpConfigTree( NamedValueTree config ) {
            IndentedStringBuilder tmpResult = new IndentedStringBuilder( );
            this.dumpConfigTree_recursive( tmpResult,
                                           config );
            return tmpResult.getResult( );
        }

        protected void dumpConfigPlain_recursive( IndentedStringBuilder result,
                                                  String parentPath,
                                                  NamedValueTree config ) {
            String tmpPath = ( parentPath == null ) ? config.getName( )
                                                    : String.format( "%s.%s",
                                                                     parentPath,
                                                                     config.getName( ) );
            result.append( "%s: %s",
                           tmpPath,
                           StringHelper.nullText( config.getValue( ) ) );
            if ( config.branches != null ) {
                for ( NamedValueTree tmpConfig : config.branches ) {
                    dumpConfigPlain_recursive( result,
                                               tmpPath,
                                               tmpConfig );
                }
            }
        }

        protected String dumpConfigPlain( NamedValueTree config ) {
            IndentedStringBuilder tmpResult = new IndentedStringBuilder( );
            this.dumpConfigPlain_recursive( tmpResult,
                                            null,
                                            config );
            return tmpResult.getResult( );
        }

        @Override
        protected void _entry_point( String[ ] args )
            throws Throwable {
            NamedValueTree tmpConfig = this.createStatic( );
            log.info( this.dumpConfigTree( tmpConfig ) );
            log.info( this.dumpConfigPlain( tmpConfig ) );
            //
            String tmpConfigXml = tmpConfig.toXml( );
            log.info( tmpConfigXml );
            //
            NamedValueTree tmpConfigRecover = NamedValueTree.fromXml( tmpConfigXml );
            log.info( this.dumpConfigTree( tmpConfigRecover ) );
            log.info( this.dumpConfigPlain( tmpConfigRecover ) );
            //
            for ( NamedValueTree tmpTree : new NamedValueTree[ ] { tmpConfigRecover.branch( "myOwn.tree1" ),
                                                                   tmpConfigRecover.branch( "myOwn.tree2" ) } ) {
                tmpTree.branch( "branch1" );
                tmpTree.branch( "branch1.twig" ).set( "twigValue" );
                tmpTree.branch( "branch1.twig.leaf",
                                "leafValue" );
                tmpTree.branch( "branch1.twig.fruit" ).set( "fruitValue" );
            }
            log.info( this.dumpConfigTree( tmpConfigRecover ) );
            log.info( this.dumpConfigPlain( tmpConfigRecover ) );
            log.info( tmpConfigRecover.toXml( ) );
        }

        public static void main( String[ ] args ) {
            try {
                AbstractConsoleApplication.execute( true,
                                                    Test.class,
                                                    args );
            }
            catch ( Throwable e ) {
                log.exception( e );
            }
        }
    }
}
