package org.dmc.vottdotserver.models.domain.vottdot;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExportFormat {
    private String providerType;
    private ExportFormatProviderOption providerOptions;
}
