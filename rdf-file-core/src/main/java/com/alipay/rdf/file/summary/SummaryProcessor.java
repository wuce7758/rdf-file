package com.alipay.rdf.file.summary;

import java.util.ArrayList;
import java.util.List;

import com.alipay.rdf.file.exception.RdfErrorEnum;
import com.alipay.rdf.file.exception.RdfFileException;
import com.alipay.rdf.file.model.Summary;
import com.alipay.rdf.file.model.SummaryPair;
import com.alipay.rdf.file.processor.ProcessCotnext;
import com.alipay.rdf.file.processor.ProcessorTypeEnum;
import com.alipay.rdf.file.spi.RdfFileProcessorSpi;
import com.alipay.rdf.file.spi.RdfFileSummaryPairSpi;
import com.alipay.rdf.file.util.BeanMapWrapper;
import com.alipay.rdf.file.util.RdfFileConstants;

/**
 * Copyright (C) 2013-2018 Ant Financial Services Group
 *
 * @author hongwei.quhw
 * @version $Id: SummaryProcessor.java, v 0.1 2018年3月12日 下午4:28:04 hongwei.quhw Exp $
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SummaryProcessor implements RdfFileProcessorSpi {

    @Override
    public List<ProcessorTypeEnum> supportedTypes() {
        List<ProcessorTypeEnum> types = new ArrayList<ProcessorTypeEnum>();
        types.add(ProcessorTypeEnum.AFTER_READ_HEAD);
        types.add(ProcessorTypeEnum.AFTER_READ_ROW);
        types.add(ProcessorTypeEnum.AFTER_READ_TAIL);
        types.add(ProcessorTypeEnum.AFTER_WRITE_HEAD);
        types.add(ProcessorTypeEnum.AFTER_WRITE_ROW);
        types.add(ProcessorTypeEnum.AFTER_WRITE_TAIL);
        return types;
    }

    @Override
    public void process(ProcessCotnext pc) {
        ProcessorTypeEnum processorType = pc.getProcessorType();

        Summary summary = (Summary) pc.getBizData(RdfFileConstants.SUMMARY);
        Object data = pc.getBizData(RdfFileConstants.DATA);

        if (null == data) {
            return;
        }

        BeanMapWrapper bmw = new BeanMapWrapper(data);
        switch (processorType) {
            case AFTER_READ_HEAD:
            case AFTER_WRITE_HEAD:
                List<SummaryPair> summaryPairs = summary.getHeadSummaryPairs();
                for (SummaryPair pair : summaryPairs) {
                    Object headValue = bmw.getProperty(pair.getHeadKey());
                    ((RdfFileSummaryPairSpi) pair).setHeadValue(headValue);
                }
                break;
            case AFTER_READ_ROW:
            case AFTER_WRITE_ROW:
                summary.addTotalCount(1);

                summaryPairs = summary.getSummaryPairs();
                for (SummaryPair pair : summaryPairs) {
                    Object colValue = bmw.getProperty(pair.getColumnKey());
                    ((RdfFileSummaryPairSpi) pair).addColValue(colValue);
                }
                break;
            case AFTER_READ_TAIL:
            case AFTER_WRITE_TAIL:
                summaryPairs = summary.getTailSummaryPairs();
                for (SummaryPair pair : summaryPairs) {
                    Object tailValue = bmw.getProperty(pair.getTailKey());
                    ((RdfFileSummaryPairSpi) pair).setTailValue(tailValue);
                }
                break;
            default:
                throw new RdfFileException(
                    "SummaryReadProcessor 不支持ProcessorTypeEnum=" + processorType.name(),
                    RdfErrorEnum.UNSUPPORTED_OPERATION);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
