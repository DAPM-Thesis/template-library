package librarytemplate;

import candidate_validation.ValidatedPipeline;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import pipeline.PipelineBuilder;
import pipeline.service.PipelineExecutionService;
import repository.TemplateRepository;
import templates.WikipediaEventSource;
import templates.beamline.CausalNetVisualizer;
import templates.beamline.ProcessMapVisualizer;
import templates.beamline.TrivialMiner;
import templates.beamline.heuristicsminer.HeuristicsMinerLossyCounting;
import templates.beamline.heuristicsminer.budget.HeuristicsMinerBudgetLossyCounting;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
@ComponentScan(basePackages = {"controller", "pipeline", "communication", "repository"})
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

        TemplateRepository templateRepository = context.getBean(TemplateRepository.class);
        initializeTemplateRepository(templateRepository);

        String pipelineID = "lib_example_pipeline";
        String contents;
        try { contents = Files.readString(Paths.get("src/main/pipeline_representations/heuristics_miner_pipeline.json")); } catch (IOException e) { throw new RuntimeException(e);}

        URI configURI = Paths.get("src/main/config_schemas").toUri();
        ValidatedPipeline validatedPipeline = new ValidatedPipeline(contents, configURI);

        PipelineBuilder pipelineBuilder = context.getBean(PipelineBuilder.class);
        pipelineBuilder.buildPipeline(pipelineID, validatedPipeline);

        PipelineExecutionService executionService = context.getBean(PipelineExecutionService.class);
        executionService.start(pipelineID);
    }

    private static void initializeTemplateRepository(TemplateRepository templateRepository) {
        templateRepository.storeTemplate("TrivialMiner", TrivialMiner.class);
        templateRepository.storeTemplate("ProcessMapVisualizer", ProcessMapVisualizer.class);
        templateRepository.storeTemplate("WikipediaEventSource", WikipediaEventSource.class);
        templateRepository.storeTemplate("CausalNetVisualizer", CausalNetVisualizer.class);
        templateRepository.storeTemplate("HeuristicsMinerLossyCounting", HeuristicsMinerLossyCounting.class);
        templateRepository.storeTemplate("HeuristicsMinerBudgetLossyCounting", HeuristicsMinerBudgetLossyCounting.class);
    }
}
