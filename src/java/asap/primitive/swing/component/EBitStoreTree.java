package asap.primitive.swing.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import asap.primitive.bits.bitStore.BitStoreData;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreItemData;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreRecordData;
import asap.primitive.bits.bitStore.BitStoreData.BitStoreViewData;

public class EBitStoreTree {

    public boolean                 selectable;

    protected JTree                jTree;

    protected DefaultTreeModel     defaultTreeModel;

    protected BitStoreTreeRootNode rootNode;

    protected BitStoreTreeListener listener;

    protected int                  selectedView;

    protected boolean              sortByName;

    public EBitStoreTree( JTree itemTree,
                          boolean selectable,
                          BitStoreTreeListener listener ) {
        this.jTree = itemTree;
        this.selectable = selectable;
        this.listener = listener;
        this.selectedView = ( -1 );
        this.sortByName = false;
        //
        this.defaultTreeModel = new DefaultTreeModel( null );
        this.jTree.setModel( this.defaultTreeModel );
        this.jTree.setCellRenderer( new BitStoreTreeCellRenderer( ) );
        this.jTree.setCellEditor( new BitStoreTreeCellEditor( this.jTree ) );
        this.jTree.setEditable( selectable );
        this.jTree.getSelectionModel( ).setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
        ToolTipManager.sharedInstance( ).registerComponent( this.jTree );
        this.jTree.addTreeSelectionListener( new TreeSelectionListener( ) {

            @Override
            public void valueChanged( TreeSelectionEvent event ) {
                if ( EBitStoreTree.this.listener != null ) {
                    DefaultMutableTreeNode tmpNode = (DefaultMutableTreeNode) EBitStoreTree.this.jTree.getLastSelectedPathComponent( );
                    EBitStoreTree.this.listener.itemSelectionChanged( ( tmpNode == null ) ? null
                                                                                          : ( (BitStoreTreeNode) tmpNode.getUserObject( ) ) );
                }
            }
        } );
    }

    protected void updateSortMode( ) {
        String tmpSelectionPath = this.getSelectionPath( );
        for ( BitStoreTreeViewNode tmpView : this.rootNode.viewNodes ) {
            tmpView.sortRecords( );
        }
        this.defaultTreeModel.reload( );
        this.setSelectionPath( tmpSelectionPath );
        if ( this.listener != null ) {
            this.listener.sortOrderChanged( this.sortByName );
        }
    }

    public void toggleSortMode( ) {
        this.sortByName ^= true;
        this.updateSortMode( );
    }

    public boolean isSortByName( ) {
        return this.sortByName;
    }

    public void setBitStore( String mediaDescription,
                             BitStoreData storeData ) {
        this.rootNode = ( storeData == null ) ? null
                                              : new BitStoreTreeRootNode( mediaDescription,
                                                                          storeData );
        this.setSelectedView( 0 );
        if ( ( storeData != null ) && this.sortByName ) {
            this.updateSortMode( );
        }
    }

    public void setSelectedView( int viewIndex ) {
        DefaultMutableTreeNode tmpTreeRootNode = null;
        if ( ( this.rootNode == null ) || ( viewIndex < 0 ) || ( viewIndex >= this.rootNode.viewNodes.size( ) ) ) {
            viewIndex = ( -1 );
        }
        else {
            tmpTreeRootNode = this.rootNode.viewNodes.get( viewIndex ).jTreeNode;
        }
        this.selectedView = viewIndex;
        this.defaultTreeModel.setRoot( tmpTreeRootNode );
    }

    public int getSelectedView( ) {
        return this.selectedView;
    }

    public String getSelectionPath( ) {
        String tmpResult = "";
        DefaultMutableTreeNode tmpTreeNode = (DefaultMutableTreeNode) EBitStoreTree.this.jTree.getLastSelectedPathComponent( );
        if ( tmpTreeNode != null ) {
            BitStoreTreeNode tmpNode = ( (BitStoreTreeNode) tmpTreeNode.getUserObject( ) );
            if ( tmpNode instanceof BitStoreTreeRecordNode ) {
                tmpResult = ( (BitStoreTreeRecordNode) tmpNode ).name;
            }
            else if ( tmpNode instanceof BitStoreTreeFieldNode ) {
                BitStoreTreeFieldNode tmpFieldNode = (BitStoreTreeFieldNode) tmpNode;
                tmpResult = String.format( "%s.%s",
                                           tmpFieldNode.parentRecordNode.name,
                                           tmpFieldNode.name );
            }
        }
        return tmpResult;
    }

    public void setSelectionPath( String path ) {
        if ( path != null ) {
            String[ ] tmpItemPath = path.split( "\\." );
            if ( tmpItemPath.length > 0 ) {
                List< Object > tmpSelectionPath = new ArrayList< Object >( );
                tmpSelectionPath.add( this.rootNode.viewNodes.get( this.selectedView ).jTreeNode );
                List< Object > tmpVisiblePath = new ArrayList< Object >( tmpSelectionPath );
                BitStoreTreeRecordNode tmpRecord = this.rootNode.viewNodes.get( this.selectedView ).getRecord( tmpItemPath[ 0 ] );
                if ( tmpRecord != null ) {
                    tmpSelectionPath.add( tmpRecord.jTreeNode );
                    tmpVisiblePath.add( tmpRecord.jTreeNode );
                    if ( tmpItemPath.length > 1 ) {
                        BitStoreTreeFieldNode tmpField = tmpRecord.getField( tmpItemPath[ 1 ] );
                        if ( tmpField != null ) {
                            tmpSelectionPath.add( tmpField.jTreeNode );
                        }
                        else {
                            tmpSelectionPath.add( tmpRecord.fieldNodes.get( 0 ).jTreeNode );
                        }
                    }
                }
                TreePath tmpItemTreePath = new TreePath( tmpSelectionPath.toArray( new Object[ 0 ] ) );
                this.jTree.setSelectionPath( tmpItemTreePath );
                this.jTree.makeVisible( tmpItemTreePath );
                Rectangle tmpVisibleBounds = this.jTree.getPathBounds( new TreePath( tmpVisiblePath.toArray( new Object[ 0 ] ) ) );
                tmpVisibleBounds.height = this.jTree.getVisibleRect( ).height;
                this.jTree.scrollRectToVisible( tmpVisibleBounds );
                return;
            }
        }
        this.jTree.setSelectionRow( 0 );
    }

    public void update( final BitStoreTreeNode node ) {
        this.defaultTreeModel.nodeChanged( node.jTreeNode );
    }

    public void update( ) {
        this.update( this.rootNode.viewNodes.get( this.selectedView ) );
    }

    public void accept( BitStoreTreeVisitor visitor ) {
        if ( visitor.startVisit( this ) ) {
            this.rootNode.accept( visitor );
        }
        visitor.endVisit( this );
    }

    public static abstract class BitStoreTreeListener {

        public void itemSelectionChanged( BitStoreTreeNode selectedNode ) {
        }

        public void itemCheckChanged( BitStoreTreeNode chagendNode ) {
        }

        public void sortOrderChanged( boolean sortOrder ) {
        }
    }

    public static abstract class BitStoreTreeVisitor {

        protected boolean defaultResult = true;

        public boolean startVisit( EBitStoreTree tree ) {
            return this.defaultResult;
        }

        public boolean startVisit( BitStoreTreeRootNode rootNode ) {
            return this.defaultResult;
        }

        public boolean startVisit( BitStoreTreeViewNode viewNode ) {
            return this.defaultResult;
        }

        public boolean startVisit( BitStoreTreeRecordNode recordNode ) {
            return this.defaultResult;
        }

        public boolean visit( BitStoreTreeFieldNode fieldNode ) {
            return this.defaultResult;
        }

        public boolean endVisit( BitStoreTreeRecordNode recordNode ) {
            return this.defaultResult;
        }

        public boolean endVisit( BitStoreTreeViewNode viewNode ) {
            return this.defaultResult;
        }

        public boolean endVisit( BitStoreTreeRootNode rootNode ) {
            return this.defaultResult;
        }

        public boolean endVisit( EBitStoreTree tree ) {
            return this.defaultResult;
        }
    }

    public static abstract class BitStoreTreeNode {

        public boolean                selectable;

        public boolean                triState;

        public ETristateState         selectionState;

        public DefaultMutableTreeNode jTreeNode;

        public Color                  foreColor;

        public BitStoreTreeNode( boolean selectable,
                                 boolean triState,
                                 DefaultMutableTreeNode parentJTreeNode ) {
            this.selectable = selectable;
            this.triState = triState;
            this.selectionState = ETristateState.DESELECTED;
            this.jTreeNode = new DefaultMutableTreeNode( this );
            this.foreColor = null;
            if ( parentJTreeNode != null ) {
                parentJTreeNode.add( this.jTreeNode );
            }
        }

        public abstract String getText( );

        public abstract String getTooltip( );

        public abstract boolean accept( BitStoreTreeVisitor visitor );
    }

    public class BitStoreTreeRootNode extends BitStoreTreeNode {

        public String                       description;

        public BitStoreData                 storeData;

        public List< BitStoreTreeViewNode > viewNodes;

        public BitStoreTreeRootNode( String description,
                                     BitStoreData storeData ) {
            super( false,
                   false,
                   null );
            this.description = description;
            this.storeData = storeData;
            this.viewNodes = new ArrayList< BitStoreTreeViewNode >( );
            for ( BitStoreViewData tmpView : storeData.getViews( ) ) {
                this.viewNodes.add( new BitStoreTreeViewNode( this,
                                                              tmpView ) );
            }
        }

        @Override
        public String getText( ) {
            return this.description;
        }

        @Override
        public String getTooltip( ) {
            return this.storeData.getMap( ).getDescription( );
        }

        @Override
        public boolean accept( BitStoreTreeVisitor visitor ) {
            if ( visitor.startVisit( this ) ) {
                for ( BitStoreTreeViewNode tmpViewNode : this.viewNodes ) {
                    if ( !( tmpViewNode.accept( visitor ) ) ) {
                        break;
                    }
                }
            }
            return visitor.endVisit( this );
        }
    }

    public class BitStoreTreeViewNode extends BitStoreTreeNode {

        public BitStoreTreeRootNode           parentRootNode;

        public BitStoreViewData               viewData;

        public List< BitStoreTreeRecordNode > recordNodes;

        public BitStoreTreeViewNode( BitStoreTreeRootNode parentRootNode,
                                     BitStoreViewData viewData ) {
            super( true,
                   true,
                   parentRootNode.jTreeNode );
            this.parentRootNode = parentRootNode;
            this.viewData = viewData;
            this.recordNodes = new ArrayList< BitStoreTreeRecordNode >( );
            for ( BitStoreRecordData tmpRecordData : this.viewData.getRecords( ) ) {
                this.recordNodes.add( new BitStoreTreeRecordNode( this,
                                                                  tmpRecordData ) );
            }
        }

        @Override
        public String getText( ) {
            return this.parentRootNode.description;
        }

        @Override
        public String getTooltip( ) {
            return this.viewData.getDescription( );
        }

        public BitStoreTreeRecordNode getRecord( String name ) {
            BitStoreTreeRecordNode tmpResult = null;
            for ( BitStoreTreeRecordNode tmpRecordNode : this.recordNodes ) {
                if ( tmpRecordNode.name.compareTo( name ) == 0 ) {
                    tmpResult = tmpRecordNode;
                    break;
                }
            }
            return tmpResult;
        }

        @Override
        public boolean accept( BitStoreTreeVisitor visitor ) {
            if ( visitor.startVisit( this ) ) {
                for ( BitStoreTreeRecordNode tmpRecordNode : this.recordNodes ) {
                    if ( !( tmpRecordNode.accept( visitor ) ) ) {
                        break;
                    }
                }
            }
            return visitor.endVisit( this );
        }

        protected void sortRecords( ) {
            Vector< BitStoreTreeRecordNode > tmpSortedRecordNodes = new Vector< BitStoreTreeRecordNode >( );
            for ( BitStoreTreeRecordNode tmpRecordNode : this.recordNodes ) {
                int tmpRecordIndex = 0;
                for ( BitStoreTreeRecordNode tmpSortedRecordNode : tmpSortedRecordNodes ) {
                    int tmpCompareResult;
                    if ( EBitStoreTree.this.sortByName ) {
                        tmpCompareResult = tmpRecordNode.name.compareToIgnoreCase( tmpSortedRecordNode.name );
                    }
                    else {
                        tmpCompareResult = ( tmpRecordNode.recordData.getIndex( )
                                             - tmpSortedRecordNode.recordData.getIndex( ) );
                    }
                    if ( tmpCompareResult < 0 ) {
                        break;
                    }
                    ++tmpRecordIndex;
                }
                tmpSortedRecordNodes.insertElementAt( tmpRecordNode,
                                                      tmpRecordIndex );
            }
            this.jTreeNode.removeAllChildren( );
            this.recordNodes.clear( );
            for ( BitStoreTreeRecordNode tmpRecordNode : tmpSortedRecordNodes ) {
                this.jTreeNode.add( tmpRecordNode.jTreeNode );
                this.recordNodes.add( tmpRecordNode );
                tmpRecordNode.sortFields( );
            }
        }
    }

    public class BitStoreTreeRecordNode extends BitStoreTreeNode {

        public BitStoreTreeViewNode          parentViewNode;

        public BitStoreRecordData            recordData;

        public List< BitStoreTreeFieldNode > fieldNodes;

        public String                        name;

        public BitStoreTreeRecordNode( BitStoreTreeViewNode parentViewNode,
                                       BitStoreRecordData recordData ) {
            super( true,
                   true,
                   parentViewNode.jTreeNode );
            this.parentViewNode = parentViewNode;
            this.recordData = recordData;
            this.fieldNodes = new ArrayList< BitStoreTreeFieldNode >( );
            for ( BitStoreItemData tmpFieldData : this.recordData.getItems( ) ) {
                this.fieldNodes.add( new BitStoreTreeFieldNode( this,
                                                                tmpFieldData ) );
            }
            this.name = this.recordData.getName( );
        }

        public String getPath( ) {
            return this.name;
        }

        @Override
        public String getText( ) {
            return this.name;
        }

        @Override
        public String getTooltip( ) {
            return this.recordData.getDescription( );
        }

        public BitStoreTreeFieldNode getField( String name ) {
            BitStoreTreeFieldNode tmpResult = null;
            for ( BitStoreTreeFieldNode tmpFieldNode : this.fieldNodes ) {
                if ( tmpFieldNode.name.compareTo( name ) == 0 ) {
                    tmpResult = tmpFieldNode;
                    break;
                }
            }
            return tmpResult;
        }

        @Override
        public boolean accept( BitStoreTreeVisitor visitor ) {
            if ( visitor.startVisit( this ) ) {
                for ( BitStoreTreeFieldNode tmpFieldNode : this.fieldNodes ) {
                    if ( !( tmpFieldNode.accept( visitor ) ) ) {
                        break;
                    }
                }
            }
            return visitor.endVisit( this );
        }

        protected void sortFields( ) {
            Vector< BitStoreTreeFieldNode > tmpSortedFieldNodes = new Vector< BitStoreTreeFieldNode >( );
            for ( BitStoreTreeFieldNode tmpField : this.fieldNodes ) {
                int tmpFieldIndex = 0;
                for ( BitStoreTreeFieldNode tmpSortedFieldNode : tmpSortedFieldNodes ) {
                    int tmpCompareResult;
                    if ( EBitStoreTree.this.sortByName ) {
                        tmpCompareResult = tmpField.name.compareToIgnoreCase( tmpSortedFieldNode.name );
                    }
                    else {
                        tmpCompareResult = tmpField.itemData.getIndex( ) - tmpSortedFieldNode.itemData.getIndex( );
                    }
                    if ( tmpCompareResult < 0 ) {
                        break;
                    }
                    ++tmpFieldIndex;
                }
                tmpSortedFieldNodes.insertElementAt( tmpField,
                                                     tmpFieldIndex );
            }
            this.fieldNodes.clear( );
            this.jTreeNode.removeAllChildren( );
            for ( BitStoreTreeFieldNode tmpFieldNode : tmpSortedFieldNodes ) {
                this.jTreeNode.add( tmpFieldNode.jTreeNode );
                this.fieldNodes.add( tmpFieldNode );
            }
        }
    }

    public class BitStoreTreeFieldNode extends BitStoreTreeNode {

        public BitStoreTreeRecordNode parentRecordNode;

        public BitStoreItemData       itemData;

        public String                 name;

        public BitStoreTreeFieldNode( BitStoreTreeRecordNode parentRecordNode,
                                      BitStoreItemData itemData ) {
            super( true,
                   false,
                   parentRecordNode.jTreeNode );
            this.itemData = itemData;
            this.parentRecordNode = parentRecordNode;
            this.name = this.itemData.getName( );
        }

        public String getPath( ) {
            return String.format( "%s.%s",
                                  this.parentRecordNode.name,
                                  this.name );
        }

        @Override
        public String getText( ) {
            return this.name;
        }

        @Override
        public String getTooltip( ) {
            return this.itemData.getDescription( );
        }

        @Override
        public boolean accept( BitStoreTreeVisitor visitor ) {
            return visitor.visit( this );
        }
    }

    @SuppressWarnings( "serial" )
    protected class BitStoreTreeCellRenderer extends DefaultTreeCellRenderer {

        protected JCheckBox               simpleCheckBoxRenderer;

        protected ETristateCheckBox       triStateCheckBoxRenderer;

        protected DefaultTreeCellRenderer nonCheckBoxRenderer = new DefaultTreeCellRenderer( );

        protected Color                   selectionForeground;

        protected Color                   selectionBackground;

        protected Color                   textForeground;

        protected Color                   textBackground;

        public BitStoreTreeCellRenderer( ) {
            this.simpleCheckBoxRenderer = new JCheckBox( );
            this.triStateCheckBoxRenderer = new ETristateCheckBox( );
            this.selectionForeground = UIManager.getColor( "Tree.selectionForeground" );
            this.selectionBackground = UIManager.getColor( "Tree.selectionBackground" );
            this.textForeground = UIManager.getColor( "Tree.textForeground" );
            this.textBackground = UIManager.getColor( "Tree.textBackground" );
        }

        @Override
        public Component getTreeCellRendererComponent( JTree tree,
                                                       Object node,
                                                       boolean selected,
                                                       boolean expanded,
                                                       boolean leaf,
                                                       int row,
                                                       boolean hasFocus ) {
            JComponent tmpResult = null;
            if ( node != null ) {
                BitStoreTreeNode tmpNode = (BitStoreTreeNode) ( (DefaultMutableTreeNode) node ).getUserObject( );
                if ( EBitStoreTree.this.selectable && tmpNode.selectable ) {
                    JCheckBox tmpCheckBox;
                    if ( tmpNode.triState ) {
                        this.triStateCheckBoxRenderer.setState( tmpNode.selectionState );
                        tmpCheckBox = this.triStateCheckBoxRenderer;
                    }
                    else {
                        this.simpleCheckBoxRenderer.setSelected( tmpNode.selectionState == ETristateState.SELECTED );
                        tmpCheckBox = this.simpleCheckBoxRenderer;
                    }
                    tmpCheckBox.setText( tmpNode.getText( ) );
                    tmpCheckBox.setToolTipText( tmpNode.getTooltip( ) );
                    tmpResult = tmpCheckBox;
                }
                else {
                    tmpResult = (JComponent) this.nonCheckBoxRenderer.getTreeCellRendererComponent( tree,
                                                                                                    tmpNode.getText( ),
                                                                                                    selected,
                                                                                                    expanded,
                                                                                                    leaf,
                                                                                                    row,
                                                                                                    hasFocus );
                }
                tmpResult.setToolTipText( tmpNode.getTooltip( ) );
                tmpResult.setEnabled( tree.isEnabled( ) );
                tmpResult.setForeground( ( tmpNode.foreColor != null ) ? tmpNode.foreColor
                                                                       : ( selected ? this.selectionForeground
                                                                                    : this.textForeground ) );
                tmpResult.setBackground( selected ? this.selectionBackground
                                                  : this.textBackground );
            }
            return tmpResult;
        }
    }

    @SuppressWarnings( "serial" )
    protected class BitStoreTreeCellEditor extends AbstractCellEditor implements TreeCellEditor {

        protected JTree                    tree;

        protected BitStoreTreeCellRenderer renderer = new BitStoreTreeCellRenderer( );

        protected BitStoreTreeNode         nodeValue;

        protected JCheckBox                checkBox;

        protected ItemListener             itemListener;

        public BitStoreTreeCellEditor( JTree tree ) {
            this.tree = tree;
            this.renderer = new BitStoreTreeCellRenderer( );
            this.itemListener = new ItemListener( ) {

                public void itemStateChanged( ItemEvent itemEvent ) {
                    if ( BitStoreTreeCellEditor.this.stopCellEditing( ) ) {
                        BitStoreTreeCellEditor.this.fireEditingStopped( );
                        if ( EBitStoreTree.this.listener != null ) {
                            EBitStoreTree.this.listener.itemCheckChanged( BitStoreTreeCellEditor.this.nodeValue );
                        }
                    }
                }
            };
        }

        @Override
        public boolean isCellEditable( EventObject event ) {
            boolean tmpResult = false;
            if ( event instanceof MouseEvent ) {
                MouseEvent tmpMouseEvent = (MouseEvent) event;
                TreePath tmpTreePath = this.tree.getPathForLocation( tmpMouseEvent.getX( ),
                                                                     tmpMouseEvent.getY( ) );
                if ( tmpTreePath != null ) {
                    Object tmpPathNode = tmpTreePath.getLastPathComponent( );
                    if ( tmpPathNode != null ) {
                        BitStoreTreeNode tmpItemNode = (BitStoreTreeNode) ( (DefaultMutableTreeNode) tmpPathNode ).getUserObject( );
                        tmpResult = tmpItemNode.selectable;
                    }
                }
            }
            return tmpResult;
        }

        @Override
        public Object getCellEditorValue( ) {
            if ( this.checkBox instanceof ETristateCheckBox ) {
                this.nodeValue.selectionState = ( (ETristateCheckBox) this.checkBox ).getState( );
            }
            else {
                this.nodeValue.selectionState = ( (JCheckBox) this.checkBox ).isSelected( ) ? ETristateState.SELECTED
                                                                                            : ETristateState.DESELECTED;
            }
            this.checkBox.removeItemListener( this.itemListener );
            return this.nodeValue;
        }

        @Override
        public Component getTreeCellEditorComponent( JTree tree,
                                                     Object value,
                                                     boolean isSelected,
                                                     boolean expanded,
                                                     boolean leaf,
                                                     int row ) {
            this.nodeValue = ( (BitStoreTreeNode) ( (DefaultMutableTreeNode) value ).getUserObject( ) );
            Component tmpEditor = this.renderer.getTreeCellRendererComponent( tree,
                                                                              value,
                                                                              true,
                                                                              expanded,
                                                                              leaf,
                                                                              row,
                                                                              true );
            // editor always selected / focused
            if ( tmpEditor instanceof JCheckBox ) {
                this.checkBox = (JCheckBox) tmpEditor;
                ( (JCheckBox) tmpEditor ).addItemListener( this.itemListener );
            }
            return tmpEditor;
        }
    }
}
