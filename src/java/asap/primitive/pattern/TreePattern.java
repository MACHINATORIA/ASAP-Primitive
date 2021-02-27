package asap.primitive.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import asap.primitive.string.StringHelper;

public class TreePattern {

    public static interface TreeItem {

        public abstract String getText( );

        public abstract void setText( String text );

        public abstract List< ? extends TreeItem > getChilds( );
    }

    public static abstract class AbstractTreeItem< T extends AbstractTreeItem< T > > implements TreeItem {

        @Override
        public String getText( ) {
            return this.text;
        }

        @Override
        public void setText( String text ) {
            this.text = ( text == null ) ? StringHelper.EMPTY
                                         : text;
        }

        @Override
        public List< T > getChilds( ) {
            return new ArrayList< T >( this.childs );
        }

        public T addChild( ) {
            return this.addChild( "" );
        }

        public T addChild( String childText ) {
            T tmpChild = this.construct( childText );
            this.childs.add( tmpChild );
            return tmpChild;
        }

        public List< T > addChilds( String... childTexts ) {
            return this.addChilds( Arrays.asList( childTexts ) );
        }

        public List< T > addChilds( List< String > childTexts ) {
            ArrayList< T > tmpChilds = new ArrayList< T >( );
            for ( String tmpText : childTexts ) {
                tmpChilds.add( this.addChild( tmpText ) );
            }
            return tmpChilds;
        }

        public T addChild( T child ) {
            this.childs.add( child );
            return child;
        }

        @SuppressWarnings( "unchecked" )
        public List< T > addChilds( T... childs ) {
            List< T > tmpChilds = Arrays.asList( childs );
            this.childs.addAll( tmpChilds );
            return tmpChilds;
        }

        public void clearChilds( ) {
            for ( T tmpChild : this.childs ) {
                tmpChild.clearChilds( );
            }
            this.childs.clear( );
        }

        protected abstract T construct( String text );

        protected AbstractTreeItem( String text ) {
            this.text = text;
            this.childs = new ArrayList< T >( );
        }

        @SuppressWarnings( "unchecked" )
        protected AbstractTreeItem( String text,
                                    T... childs ) {
            this( text );
            this.addChilds( childs );
        }

        protected AbstractTreeItem( String text,
                                    List< String > childTexts ) {
            this( text );
            this.addChilds( childTexts );
        }

        protected AbstractTreeItem( String text,
                                    String... childTexts ) {
            this( text,
                  Arrays.asList( childTexts ) );
        }

        protected AbstractTreeItem( TreeItem treeItem ) {
            this( treeItem.getText( ) );
            this.cloneChilds( treeItem.getChilds( ) );
        }

        protected AbstractTreeItem( List< ? extends TreeItem > treeItems ) {
            this( (String) null );
            this.cloneChilds( treeItems );
        }

        protected void cloneChilds( List< ? extends TreeItem > childs ) {
            for ( TreeItem tmpChild : childs ) {
                T tmpItem = this.construct( tmpChild.getText( ) );
                tmpItem.cloneChilds( tmpChild.getChilds( ) );
                this.childs.add( tmpItem );
            }
        }

        private String    text;

        private List< T > childs;
    }
}
