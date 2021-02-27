package asap.primitive.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import asap.primitive.math.NumberHelper;

public class RegexHelper {

    public static class RegexList {

        protected List< Pattern > patternList;

        public RegexList( String... regexes ) {
            this.patternList = new ArrayList< Pattern >( );
            for ( String tmpRegex : regexes ) {
                this.patternList.add( Pattern.compile( tmpRegex ) );
            }
        }

        public boolean matches( String targetString ) {
            return ( this.matchIndexes( targetString ).length > 0 );
        }

        public int[ ] matchIndexes( String targetString ) {
            List< Integer > tmpResult = new ArrayList< Integer >( );
            for ( int tmpIndex = 0; tmpIndex < this.patternList.size( ); tmpIndex++ ) {
                if ( this.patternList.get( tmpIndex ).matcher( targetString ).matches( ) ) {
                    tmpResult.add( tmpIndex );
                }
            }
            return NumberHelper.asIntArray( tmpResult );
        }
    }

    public static class RegexMap {

        protected Map< Pattern, String > patternMap;

        public RegexMap( String... regexMappings ) {
            this.patternMap = new HashMap< Pattern, String >( );
            for ( String tmpMapping : regexMappings ) {
                String[ ] tmpStringPair = tmpMapping.split( "=",
                                                            -1 );
                this.patternMap.put( Pattern.compile( tmpStringPair[ 0 ] ),
                                     tmpStringPair[ 1 ] );
            }
        }

        protected String[ ] _operate( String targetString,
                                      boolean replace,
                                      String[ ] defaultValues ) {
            String tmpValues[] = null;
            for ( Map.Entry< Pattern, String > tmpEntry : this.patternMap.entrySet( ) ) {
                Matcher tmpMatcher = tmpEntry.getKey( ).matcher( targetString );
                if ( tmpMatcher.matches( ) ) {
                    if ( replace ) {
                        tmpValues = tmpEntry.getValue( ).split( "," );
                        for ( int tmpIndex = 0; tmpIndex < tmpValues.length; tmpIndex++ ) {
                            tmpValues[ tmpIndex ] = tmpMatcher.replaceAll( tmpValues[ tmpIndex ] );
                        }
                    }
                    else {
                        tmpValues = new String[ ] { tmpEntry.getValue( ) };
                    }
                    break;
                }
            }
            return ( tmpValues != null ) ? tmpValues
                                         : defaultValues;
        }

        public String search( String key,
                              String defaultValue ) {
            String[ ] tmpValue = this._operate( key,
                                                false,
                                                new String[ ] { defaultValue } );
            return ( tmpValue == null ) ? null
                                        : tmpValue[ 0 ];
        }

        public String search( String key ) {
            return this.search( key,
                                null );
        }

        public String[ ] translate( String key ) {
            return this._operate( key,
                                  true,
                                  null );
        }
    }

    public static List< String > regexSort( List< String > strings,
                                            String regex,
                                            String replacement ) {
        List< String > tmpResult = new ArrayList< String >( strings );
        Collections.sort( tmpResult,
                          new Comparator< String >( ) {

                              protected Pattern pattern = Pattern.compile( regex );

                              @Override
                              public int compare( String string1,
                                                  String string2 ) {
                                  Matcher tmpMatcher1 = this.pattern.matcher( string1 );
                                  String tmpString1 = tmpMatcher1.matches( ) ? tmpMatcher1.replaceAll( replacement )
                                                                             : string1;
                                  Matcher tmpMatcher2 = this.pattern.matcher( string2 );
                                  String tmpString2 = tmpMatcher2.matches( ) ? tmpMatcher2.replaceAll( replacement )
                                                                             : string2;
                                  return tmpString1.compareTo( tmpString2 );
                              }
                          } );
        return tmpResult;
    }

    public static List< String > regexTransform( List< String > strings,
                                                 String regex,
                                                 String replacement ) {
        List< String > tmpResult = new ArrayList< String >( );
        Pattern tmpPattern = Pattern.compile( regex );
        for ( String tmpString : strings ) {
            Matcher tmpMatcher = tmpPattern.matcher( tmpString );
            if ( tmpMatcher.matches( ) ) {
                tmpResult.add( tmpMatcher.replaceAll( replacement ) );
            }
        }
        return tmpResult;
    }

    public static String escapeRegexString( String string ) {
        return string.replaceAll( "(\\\\|\\.|\\*|\\?|\\$|\\{|\\}|\\[|\\]|\\|)",
                                  "\\\\$1" );
    }

    public static List< String > matcherGroups( Matcher matcher ) {
        List< String > tmpGroups = new ArrayList< String >( );
        for ( int tmpGroup = 0; tmpGroup < matcher.groupCount( ); tmpGroup++ ) {
            tmpGroups.add( matcher.group( tmpGroup ) );
        }
        return tmpGroups;
    }
}
