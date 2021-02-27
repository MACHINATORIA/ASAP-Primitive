package asap.primitive.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

@SuppressWarnings( "serial" )
public class TextOutputFrame extends JFrame {

    private JPanel    contentPane;

    private JButton   buttonOk;

    private JTextPane outputTextPane;

    private JLabel    captionLabel;

    /**
     * Create the frame.
     */
    public TextOutputFrame( ) {
        setMinimumSize( new Dimension( 620,
                                       440 ) );
        setTitle( "Title" );
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        setBounds( 100,
                   100,
                   450,
                   300 );
        contentPane = new JPanel( );
        contentPane.setBorder( new EmptyBorder( 1,
                                                1,
                                                1,
                                                1 ) );
        contentPane.setLayout( new BorderLayout( 0,
                                                 0 ) );
        setContentPane( contentPane );
        JPanel panel_1 = new JPanel( );
        contentPane.add( panel_1,
                         BorderLayout.NORTH );
        panel_1.setLayout( new BorderLayout( 0,
                                             0 ) );
        captionLabel = new JLabel( "New label" );
        captionLabel.setHorizontalAlignment( SwingConstants.CENTER );
        panel_1.add( captionLabel );
        JScrollPane scrollPane = new JScrollPane( );
        contentPane.add( scrollPane,
                         BorderLayout.CENTER );
        outputTextPane = new JTextPane( );
        outputTextPane.setEditable( false );
        outputTextPane.setContentType( "text/html" );
        scrollPane.setViewportView( outputTextPane );
        JPanel panel = new JPanel( );
        contentPane.add( panel,
                         BorderLayout.SOUTH );
        buttonOk = new JButton( "Ok" );
        panel.add( buttonOk );
    }

    public JButton getButtonOk( ) {
        return buttonOk;
    }

    public JTextPane getOutputTextPane( ) {
        return outputTextPane;
    }

    public JLabel getCaptionLabel( ) {
        return captionLabel;
    }
}
