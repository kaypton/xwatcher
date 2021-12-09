package com.github.fenrir.xopenstackagent.configs;

import lombok.Getter;
import lombok.Setter;
import org.kohsuke.args4j.Option;

public class ArgsConfig {
    @Option(name="--keystone-address", usage="OpenStack keystone address")
    @Getter @Setter private String keystoneAddress = null;

    @Option(name="--openstack-admin-username", usage="OpenStack Admin Username")
    @Getter @Setter private String openstackUsername = null;

    @Option(name="--openstack-domain-name", usage="Openstack Domain Name")
    @Getter @Setter private String openstackDomainName = null;

    @Option(name="--openstack-admin-password", usage="OpenStack Admin Password")
    @Getter @Setter private String openstackPassword = null;

    @Option(name="--openstack-project-uuid", usage="OpenStack scoped project uuid")
    @Getter @Setter private String openstackProjectUUID = null;
}
