package com.digitalfactory.plotirrigationservice.service;

import com.digitalfactory.automaticirrigationsystem.enums.IrrigationStatus;
import com.digitalfactory.baseservice.service.MessageService;
import com.digitalfactory.baseservice.util.CoreMessageConstants;
import com.digitalfactory.plotirrigationservice.dao.PlotIrrigationSlotDao;
import com.digitalfactory.plotirrigationservice.dto.PlotCropDto;
import com.digitalfactory.plotirrigationservice.dto.PlotIrrigationSlotDto;
import com.digitalfactory.plotirrigationservice.model.PlotIrrigationSlot;
import com.digitalfactory.plotirrigationservice.transformer.PlotIrrigationSlotTransformer;
import com.digitalfactory.plotirrigationservice.validator.PlotIrrigationSlotValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PlotIrrigationSlotServiceImpl implements PlotIrrigationSlotService{

    private final PlotIrrigationSlotTransformer plotIrrigationSlotTransformer;
    private final PlotIrrigationSlotDao plotIrrigationSlotDao;
    private final PlotCropService plotCropService;
    private final PlotIrrigationSlotValidator plotIrrigationSlotValidator;
    private final MessageService messageService;

    public PlotIrrigationSlotServiceImpl(PlotIrrigationSlotTransformer plotIrrigationSlotTransformer,
                                         PlotIrrigationSlotDao plotIrrigationSlotDao, @Lazy PlotCropService plotCropService,
                                         PlotIrrigationSlotValidator plotIrrigationSlotValidator, MessageService messageService) {
        this.plotIrrigationSlotTransformer = plotIrrigationSlotTransformer;
        this.plotIrrigationSlotDao = plotIrrigationSlotDao;
        this.plotCropService = plotCropService;
        this.plotIrrigationSlotValidator = plotIrrigationSlotValidator;
        this.messageService = messageService;
    }
    @Override
    public PlotIrrigationSlot doBeforeCreateEntity(PlotIrrigationSlot entity, PlotIrrigationSlotDto dto) {
        log.info("PlotIrrigationSlotService: doBeforeCreateEntity was called");
        entity.setPlotCrop(plotCropService.findEntityById(dto.getPlotCropId()).get());
        entity.setIrrigationStatus(IrrigationStatus.PENDING);
        if (plotIrrigationSlotValidator.isExists(dto)) {
            throw new EntityExistsException(getLocaleMessage(CoreMessageConstants.SLOT_ALREADY_ASSIGNED));
        }
        return entity;
    }

    @Override
    public PlotIrrigationSlot doBeforeUpdateEntity(PlotIrrigationSlot entity, PlotIrrigationSlotDto dto) {
        dto.setPlotCropId(entity.getPlotCropId());
        if (plotIrrigationSlotValidator.isExistsExcludeId(dto)) {
            throw new EntityExistsException(CoreMessageConstants.SLOT_ALREADY_ASSIGNED);
        }
        return entity;
    }

    @Override
    @Transactional
    public List<PlotIrrigationSlotDto> updateIrrigationSlots(Long plotCropId, Set<PlotIrrigationSlotDto> plotIrrigationSlotDTOS) {
        log.info("PlotIrrigationSlotService: updateIrrigationSlots was called");
        PlotCropDto plotCropDto = plotCropService.findById(plotCropId);
        plotIrrigationSlotDTOS.forEach(plotIrrigationSlotDTO -> {
            plotIrrigationSlotDTO.setPlotCropId(plotCropDto.getId());
            if (plotCropDto.getPlotIrrigationSlots().contains(plotIrrigationSlotDTO))
                update(plotIrrigationSlotDTO, plotIrrigationSlotDTO.getId());
            else create(plotIrrigationSlotDTO);
        });
        plotCropDto.getPlotIrrigationSlots().stream()
                .filter(dto -> plotIrrigationSlotDTOS.stream().noneMatch(dto2 -> Objects.equals(dto2.getId(), dto.getId())))
                .collect(Collectors.toList()).stream().forEach(this::removeSlot);
        return new ArrayList<>(plotIrrigationSlotDTOS);
    }


    private void removeSlot(PlotIrrigationSlotDto plotIrrigationSlotDTO) {
        log.info("PlotIrrigationSlotService: removeSlot was called");

        plotIrrigationSlotDTO.setMarkedAsDeleted(true);
        update(plotIrrigationSlotDTO, plotIrrigationSlotDTO.getId());
    }

    @Override
    public List<PlotIrrigationSlotDto> findAllByPlotCropId(Long plotCropId) {
        return getTransformer().transformEntityToDTO(getDao().findAllByPlotCropId(plotCropId));
    }
    @Override
    public PlotIrrigationSlotTransformer getTransformer() {
        return plotIrrigationSlotTransformer;
    }

    @Override
    public PlotIrrigationSlotDao getDao() {
        return plotIrrigationSlotDao;
    }

    @Override
    public MessageService getMessageService() {
        return messageService;
    }
}
