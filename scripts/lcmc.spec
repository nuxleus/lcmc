%global		_name LCMC

Name:		lcmc
Version:	1.0.1
Release:	1
Summary:	Pacemaker/KVM/DRBD HA Cluster GUI
Group:          Applications/System
License:	GPLv2+
URL:		http://lcmc.sourceforge.net

Source0:	http://sourceforge.net/projects/lcmc/files/%{_name}-source-1.0.1.tar.gz
Source1:	lcmc.startup.script
Source2:	lcmc.desktop
Source3:	lcmc.applications

#Source4:	build.xml

BuildRequires:	ant, jpackage-utils >= 1.5
Requires:	 java >= 0:1.6.0
BuildRequires:	 java-devel >= 0:1.6.0
BuildRequires:	 desktop-file-utils
Requires(post):	 desktop-file-utils
Requires(postun):	desktop-file-utils
BuildArch:	noarch


%description 
Linux Cluster Management Console.

%files
%doc
%{_datadir}/*
%{_datadir}/application-registry/*
%{_datadir}/pixmaps/lcmc.png
%{_datadir}/lcmc
%{_datadir}/icons/hicolor/32x32/apps/lcmc.png
%{_bindir}/lcmc
%{_datadir}/lcmc

#--------------------------------------------------------------------

%prep
%setup -q -c

%build
mkdir -p build/libs
build-jar-repository -p build/libs
cd lcmc
%ant jar

%install
rm -rf $RPM_BUILD_ROOT
install -d $RPM_BUILD_ROOT%{_datadir}
install -d $RPM_BUILD_ROOT%{_bindir}
install -pm 644 lcmc/build/jar/LCMC.jar $RPM_BUILD_ROOT%{_datadir}/LCMC.jar
install -p -D -m 0755 %{SOURCE1} $RPM_BUILD_ROOT%{_bindir}/lcmc

mkdir -p $RPM_BUILD_ROOT%{_datadir}/lcmc
mkdir -p $RPM_BUILD_ROOT%{_datadir}/pixmaps
mkdir -p $RPM_BUILD_ROOT%{_datadir}/icons/hicolor/32x32/apps
install -m 644 lcmc/src/java/lcmc/images/Icons/32x32/lcmc_icon_32x32.png $RPM_BUILD_ROOT%{_datadir}/pixmaps/lcmc.png
install -m 644 lcmc/src/java/lcmc/images/Icons/32x32/lcmc_icon_32x32.png $RPM_BUILD_ROOT%{_datadir}/icons/hicolor/32x32/apps/lcmc.png

mkdir -p $RPM_BUILD_ROOT%{_datadir}/applications
desktop-file-install \
       --dir ${RPM_BUILD_ROOT}%{_datadir}/applications \
	%{SOURCE2}

mkdir -p $RPM_BUILD_ROOT%{_datadir}/application-registry
install -m644 %{SOURCE3} $RPM_BUILD_ROOT%{_datadir}/application-registry