/*
 * This file is part of DRBD Management Console by LINBIT HA-Solutions GmbH
 * written by Rasto Levrinc.
 *
 * Copyright (C) 2009, LINBIT HA-Solutions GmbH.
 *
 * DRBD Management Console is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * DRBD Management Console is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with drbd; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package drbd.configs;

import java.util.Arrays;

/**
 * Here are commands for all fedoras.
 */
public final class DistResource_fedora extends java.util.ListResourceBundle {

    /** Get contents. */
    @Override protected Object[][] getContents() {
        return Arrays.copyOf(contents, contents.length);
    }

    /** Contents. */
    private static Object[][] contents = {
        {"Support", "fedora"},
        {"distribution", "redhat"},
        {"version:Fedora release 10*", "10"},
        {"version:Fedora release 11*", "11"},
        {"version:Fedora release 12*", "12"},
        {"version:Fedora release 13*", "13"},

        /* directory capturing regexp on the website from the kernel version */
        {"kerneldir", "(\\d+\\.\\d+\\.\\d+-\\d+.*?fc\\d+).*"},

        {"DrbdInst.install",
         DistResource.SUDO + "/bin/rpm -Uvh /tmp/drbdinst/@DRBDPACKAGES@"},

        /* DRBD native */
        {"DrbdInst.install.text.1",
         "the Fedora way"},

        {"DrbdInst.install.1",
         "yum -y install drbd-utils drbd-udev "
         + "&& if ( rpm -qa|grep pacemaker ); then"
         + " yum -y install drbd-pacemaker; fi"},
        {"DrbdInst.install.method.1",       ""},

        /* Heartbeat/Pacemaker native */
        {"HbPmInst.install.text.1",
         "the Fedora way: 1.1.x/3.0.x"},

        {"HbPmInst.install.1",
         "yum -y install pacemaker heartbeat "
         + "&& if ( rpm -qa|grep drbd ); then"
         + " yum -y install drbd-pacemaker; fi"},
        /* at least fedora 10 and fedora11 in version 2.1.3 and 2.14 has different
           ocf path. */
        {"Heartbeat.2.1.4.getOCFParameters",
         "export OCF_RESKEY_vmxpath=a;export OCF_ROOT=/usr/share/ocf;"
         + "mv /usr/lib/ocf/resource.d/linbit"
         + " /usr/share/ocf/resource.d/ 2>/dev/null;"
         + "for prov in `ls -1 /usr/share/ocf/resource.d/`; do "
         +  "for s in `ls -1 /usr/share/ocf/resource.d/$prov/ `; do "
         +  "echo -n 'provider:'; echo $prov;"
         +  "echo -n 'master:';"
         +  "grep -wl crm_master /usr/share/ocf/resource.d/$prov/$s;echo;"
         +  "/usr/share/ocf/resource.d/$prov/$s meta-data 2>/dev/null; done;"
         + "done;"
         + "echo 'provider:heartbeat';"
         + "echo 'master:';"
         + "@GUI-HELPER@ get-old-style-resources;"
         + "@GUI-HELPER@ get-lsb-resources"},

        /* Corosync/Pacemaker native */
        {"PmInst.install.text.1",
         "the Fedora way: 1.1.x/1.3.x"},
        {"PmInst.install.1",
         "yum -y install pacemaker corosync"
         + "&& if ( rpm -qa|grep drbd ); then"
         + " yum -y install drbd-pacemaker; fi"
         + " && if [ -e /etc/corosync/corosync.conf ]; then"
         + "  mv /etc/corosync/corosync.conf /etc/corosync/corosync.conf.orig;"
         + " fi"
         + " && (/sbin/chkconfig --del heartbeat;"
         + " /sbin/chkconfig --level 2345 corosync on"
         + " && /sbin/chkconfig --level 016 corosync off)"},
        /* corosync/pacemaker from source */
        {"PmInst.install.text.9",
         "from source: latest/1.2.x"},

        {"PmInst.install.staging.9", "true"},

        {"PmInst.install.9",
         "export LCRSODIR=/usr/libexec/lcrso;"
         + "export CLUSTER_USER=hacluster;"
         + "export CLUSTER_GROUP=haclient;"
         + "/usr/bin/yum -y install autoconf automake libtool glib2-devel"
         + " libxml2-devel bzip2-devel libtool-ltdl-devel e2fsprogs-devel"
         + " net-snmp-devel subversion libxslt-devel libuuid-devel"
         + " && /bin/mkdir -p /tmp/pminst "
         /* cluster glue */
         + " && /usr/bin/wget -N -O /tmp/pminst/cluster-glue.tar.bz2"
         + " http://hg.linux-ha.org/glue/archive/tip.tar.bz2"
         + " && cd /tmp/pminst"
         + " && /bin/tar xfjp cluster-glue.tar.bz2"
         + " && cd `ls -dr Reusable-Cluster-Components-*`"
         + " && ./autogen.sh && ./configure"
         + " --with-daemon-user=${CLUSTER_USER}"
         + " --with-daemon-group=${CLUSTER_GROUP}"
         + " --disable-fatal-warnings"
         + " --sysconfdir=/etc --localstatedir=/var"
         + " && make && make install"
         /* resource agents */
         + " && /usr/bin/wget -N -O /tmp/pminst/resource-agents.tar.bz2"
         + " http://hg.linux-ha.org/agents/archive/tip.tar.bz2"
         + " && cd /tmp/pminst"
         + " && /bin/tar xfjp resource-agents.tar.bz2"
         + " && cd `ls -dr Cluster-Resource-Agents-*`"
         + " && ./autogen.sh && ./configure"
         + " --sysconfdir=/etc --localstatedir=/var"
         + " && make && make install"
         /* corosync */
         + " && cd /tmp/pminst"
         + " && svn co"
         + " http://svn.fedorahosted.org/svn/corosync/branches/flatiron"
         + " && cd /tmp/pminst/flatiron"
         + " && ./autogen.sh"
         + " && ./configure --with-lcrso-dir=$LCRSODIR"
         + " --sysconfdir=/etc --localstatedir=/var"
         + " && make"
         + " && make install"
         + " && (cp init/generic /etc/init.d/corosync"
         + "    || cp init/redhat /etc/init.d/corosync)"
         + " && chmod a+x /etc/init.d/corosync"
         + " && (groupadd ais;"
         + " useradd -g ais --shell /bin/false ais;"
         + " groupadd haclient;"
         + " useradd -g haclient --shell /bin/false hacluster;"
         + " true)"
         + " && (/sbin/chkconfig --del heartbeat;"
         + " /sbin/chkconfig --level 2345 corosync on"
         + " && /sbin/chkconfig --level 016 corosync off)"
         /* pacemaker */
         + " && /usr/bin/wget -N -O /tmp/pminst/pacemaker.tar.bz2"
         + " http://hg.clusterlabs.org/pacemaker/1.1/archive/tip.tar.bz2"
         + " && cd /tmp/pminst"
         + " && /bin/tar xfjp pacemaker.tar.bz2"
         + " && cd `ls -dr Pacemaker-1-*`"
         + " && echo 'docdir = ${datadir}/doc/${PACKAGE}'>>doc/Makefile.am"
         + " && echo 'docdir = ${datadir}/doc/${PACKAGE}'>>Makefile.am"
         + " && ./autogen.sh"
         + " && ./configure --with-lcrso-dir=$LCRSODIR"
         + " --with-ais --sysconfdir=/etc --localstatedir=/var"
         + " --disable-fatal-warnings"
         + " && make && make install"
         + " && if [ -e /etc/corosync/corosync.conf ]; then"
         + " mv /etc/corosync/corosync.conf /etc/corosync/corosync.conf.orig; fi"},

        {"HbCheck.version",
         DistResource.SUDO + "@GUI-HELPER@ get-cluster-versions;"
         + "/bin/rpm -q -i openais|perl -lne"
         + " 'print \"ais:$1\" if /^Version\\s+:\\s+(\\S+)/';"
         + "/bin/rpm -q -i corosync|perl -lne"
         + " 'print \"cs:$1\" if /^Version\\s+:\\s+(\\S+)/'"},

        {"Heartbeat.deleteFromRc",
         DistResource.SUDO + "/sbin/chkconfig --del heartbeat"},

        {"Heartbeat.addToRc",
         DistResource.SUDO + "/sbin/chkconfig --add heartbeat"},

        {"Corosync.addToRc",
         DistResource.SUDO + "/sbin/chkconfig --level 2345 corosync on "
         + "&& " + DistResource.SUDO + "/sbin/chkconfig --level 016 corosync off"},

        {"Corosync.deleteFromRc",
         DistResource.SUDO + "/sbin/chkconfig --del corosync"},

        {"Openais.addToRc",
         DistResource.SUDO + "/sbin/chkconfig --level 2345 openais on "
         + "&& " + DistResource.SUDO + "/sbin/chkconfig --level 016 openais off"},

        {"Openais.deleteFromRc",
         DistResource.SUDO + "/sbin/chkconfig --del openais"},
    };
}
