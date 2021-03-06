package lcmc.gui;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import lcmc.utilities.TestSuite1;
import lcmc.utilities.Tools;
import lcmc.data.Host;

public final class ClusterBrowserTest1 extends TestCase {
    @Before
    protected void setUp() {
        TestSuite1.initTest();
    }

    @After
    protected void tearDown() {
        assertEquals("", TestSuite1.getStdout());
    }

    /* ---- tests ----- */

    @Test
    public void testProcessClusterOutput() {
        if (TestSuite1.QUICK) {
            return;
        }
        final List<String> files = new ArrayList<String>();
        final String userHome = System.getProperty("user.home");
        files.add(userHome + "/testdir/empty.xml");
        for (final String dirName : new String[]{
                    userHome + "/testdir/pacemaker/shell/regression",
                    userHome + "/testdir/pacemaker/pengine/test10"}) {
            final File dir = new File(dirName);
            assertFalse(dir == null);
            for (final File f : dir.listFiles()) {
                final String file = f.getAbsolutePath();
                if (file.length() > 3
                    && file.substring(file.length() - 4).equals(".xml")) {
                    files.add(file);
                }
            }
        }
        int i = 0;
        String emptyCib = null;
        final StringBuilder nodes = new StringBuilder("<nodes>\n");
        for (final Host host : TestSuite1.getHosts()) {
            nodes.append("  <node id=\"");
            nodes.append(host.getName());
            nodes.append("\" uname=\"");
            nodes.append(host.getName());
            nodes.append("\" type=\"normal\"/>\n");
        }
        nodes.append("</nodes>\n");
        final StringBuilder status = new StringBuilder("<status>\n");
        for (final Host host : TestSuite1.getHosts()) {
            status.append("<node_state id=\"");
            status.append(host.getName());
            status.append("\" uname=\"");
            status.append(host.getName());
            status.append("\" ha=\"active\" in_ccm=\"true\" crmd=\"online\" "
                          + "join=\"member\" expected=\"member\" "
                          + "crm-debug-origin=\"do_update_resource\" "
                          + "shutdown=\"0\">");

            status.append("</node_state>\n");
        }
        status.append("</status>\n");

        for (final String file : files) {
            i++;
            if (i > 100) {
                break;
            }
            Tools.startProgressIndicator(i + ": " + file);
            String xml = Tools.loadFile(file, true);
            xml = xml.replaceAll("<nodes/>", nodes.toString())
                     .replaceAll("<nodes>.*?</nodes>", nodes.toString())
                     .replaceAll("<status>.*?</status>", status.toString())
                     .replaceAll("<status/>", status.toString())
                     .replaceAll("<\\?.*?\\?>", "");


            final String cib = "---start---\n"
                             + "res_status\n"
                             + "ok\n"
                             + "\n"
                             + ">>>res_status\n"
                             + "cibadmin\n"
                             + "ok\n"
                             + "<pcmk>\n"
                             + xml
                             + "</pcmk>\n"
                             + ">>>cibadmin\n"
                             + "---done---\n";
            if (i == 1) {
                emptyCib = cib;
            }
            final CountDownLatch firstTime = new CountDownLatch(0);
            final boolean testOnly = false;
            for (final Host host : TestSuite1.getHosts()) {
                final ClusterBrowser cb = host.getBrowser().getClusterBrowser();
                cb.processClusterOutput(cib,
                                        new StringBuilder(""),
                                        host,
                                        firstTime,
                                        testOnly);
                Tools.waitForSwing();
                cb.getHeartbeatGraph().repaint();
            }
            Tools.sleep(1000);
            Tools.stopProgressIndicator(i + ": " + file);
            for (final Host host : TestSuite1.getHosts()) {
                final ClusterBrowser cb = host.getBrowser().getClusterBrowser();
                Tools.waitForSwing();
                cb.processClusterOutput(emptyCib,
                                        new StringBuilder(""),
                                        host,
                                        firstTime,
                                        testOnly);
                Tools.waitForSwing();
            }
            if (i % 10 == 0) {
                Tools.sleep(5000);
            }
            Tools.sleep(250);
        }
    }
}
