package com.jaspersoft.android.jaspermobile.data.mapper.job;


import com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleForm;

import java.util.Arrays;
import java.util.List;

import static com.jaspersoft.android.jaspermobile.domain.entity.job.JobScheduleForm.OutputFormat.*;

/**
 * @author Tom Koptel
 * @since 2.5
 */
class JobScheduleFormatsFactory {
    List<JobScheduleForm.OutputFormat> generate() {
        return Arrays.asList(
                CSV,
                DOCX,
                XLS_NOPAG,
                XLS,
                HTML,
                ODS,
                ODT,
                PDF,
                RTF,
                XLSX_NOPAG,
                XLSX,
                PPTX);
    }
}
