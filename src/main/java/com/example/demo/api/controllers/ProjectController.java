package com.example.demo.api.controllers;

import com.example.demo.api.dto.AskDto;
import com.example.demo.api.dto.ProjectDto;
import com.example.demo.api.exceptions.BadRequestException;
import com.example.demo.api.exceptions.NotFoundException;
import com.example.demo.api.factories.ProjectDtoFactory;
import com.example.demo.store.entities.ProjectEntity;
import com.example.demo.store.repositories.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Transactional
@RestController
public class ProjectController {

    private final ProjectRepository projectRepository;

    private final ProjectDtoFactory projectDtoFactory;

    public static final String FETCH_PROJECT = "/api/projects";
    public static final String CREATE_PROJECT = "/api/projects";
    public static final String EDIT_PROJECT = "/api/project/{project_id}";
    public static final String DELETE_PROJECT = "/api/projects/{project_id}";

    public static final String CREATE_OR_UPDATE_PROJECT = "/api/projects";

    @GetMapping(FETCH_PROJECT)
    public List<ProjectDto> fetchProjects(
            @RequestParam(value = "prefix_name", required = false)Optional<String> optionalPrefixName) {

        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

        Stream<ProjectEntity> projectStream = optionalPrefixName
                .map(projectRepository::streamAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepository::streamAll);



        return projectStream
                .map(projectDtoFactory::makeProjectDto)
                .collect(Collectors.toList());
    }

    @PostMapping(CREATE_PROJECT)
    public ProjectDto createProject(@RequestParam("project_name") String projectName) {

        if (projectName.trim().isEmpty()) {
            throw new BadRequestException("Name can't be empty.");
        }

        projectRepository
                .findByName(projectName)
                .ifPresent(project -> {
                    throw new BadRequestException(String.format("Project \"%s\" already exists", projectName));
                });

        ProjectEntity project = projectRepository.saveAndFlush(
                ProjectEntity.builder()
                        .name(projectName)
                        .build()
        );

        return projectDtoFactory.makeProjectDto(project);
    }

    @PutMapping(CREATE_OR_UPDATE_PROJECT)
    public ProjectDto createOrUpdateProject(
            @RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId,
            @RequestParam(value = "project_name", required = false) Optional<String> projectName
            //Another params..
            ) {

        //TODO:this
        return new ProjectDto();

    }

    @PatchMapping(EDIT_PROJECT)
    public ProjectDto editProject(@RequestParam String name,
                                  @PathVariable("project_id") Long projectId) {

        if (name.trim().isEmpty()) {
            throw new BadRequestException("Name can't be empty.");
        }


        ProjectEntity project = getProjectOrThrowException(projectId);

        projectRepository
                .findByName(name)
                .filter(anotherProject -> !Objects.equals(anotherProject.getId(), projectId))
                .ifPresent(anotherProject -> {
                    throw new BadRequestException(String.format("Project \"%s\" already exists", name));
                });

        project.setName(name);

       project = projectRepository.saveAndFlush(project);

        return projectDtoFactory.makeProjectDto(project);
    }

    @DeleteMapping(DELETE_PROJECT)
    public AskDto deleteProject(@PathVariable("project_id") Long projectId) {

        getProjectOrThrowException(projectId);

        projectRepository.deleteById(projectId);

        return AskDto.makeDefault(true);
    }

    private ProjectEntity getProjectOrThrowException(Long projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "Project with '%s' doesn't exist",
                                        projectId
                                )
                        )
                );
    }
}
