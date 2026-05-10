package com.example.plugins

import com.example.application.usecase.auth.GetCurrentUserUseCase
import com.example.application.usecase.auth.LoginUserUseCase
import com.example.application.usecase.auth.RegisterUserUseCase
import com.example.application.usecase.dictionary.GetKanjiDetailsUseCase
import com.example.application.usecase.dictionary.GetWordDetailsUseCase
import com.example.application.usecase.dictionary.SearchKanjiUseCase
import com.example.application.usecase.dictionary.SearchWordsUseCase
import com.example.application.usecase.learning.GetLearningBlockDetailsUseCase
import com.example.application.usecase.learning.GetLearningBlocksUseCase
import com.example.application.usecase.progress.GetKanjiProgressUseCase
import com.example.application.usecase.progress.GetWordProgressUseCase
import com.example.application.usecase.progress.UpdateKanjiProgressUseCase
import com.example.application.usecase.progress.UpdateWordProgressUseCase
import com.example.application.usecase.review.CheckKanjiAnswerUseCase
import com.example.application.usecase.review.CheckWordAnswerUseCase
import com.example.application.usecase.review.GetReviewKanjiUseCase
import com.example.application.usecase.review.GetReviewWordsUseCase
import com.example.domain.repository.KanjiRepository
import com.example.domain.repository.LearningBlockRepository
import com.example.domain.repository.ProgressRepository
import com.example.domain.repository.UserRepository
import com.example.domain.repository.WordRepository
import com.example.infra.database.repository.ExposedKanjiRepository
import com.example.infra.database.repository.ExposedLearningBlockRepository
import com.example.infra.database.repository.ExposedProgressRepository
import com.example.infra.database.repository.ExposedUserRepository
import com.example.infra.database.repository.ExposedWordRepository
import com.example.infra.security.JwtConfig
import com.example.infra.security.JwtTokenProvider
import com.example.infra.security.PasswordHasher
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies

fun Application.configureDI() {
    dependencies {
        provide<WordRepository> { ExposedWordRepository() }
        provide<KanjiRepository> { ExposedKanjiRepository() }
        provide<UserRepository> { ExposedUserRepository() }
        provide<LearningBlockRepository> { ExposedLearningBlockRepository() }
        provide<ProgressRepository> { ExposedProgressRepository() }

        provide<PasswordHasher> { PasswordHasher() }
        provide<JwtTokenProvider> { JwtTokenProvider(JwtConfig.load()) }

        provide<SearchWordsUseCase> { SearchWordsUseCase(resolve()) }
        provide<GetWordDetailsUseCase> { GetWordDetailsUseCase(resolve()) }
        provide<SearchKanjiUseCase> { SearchKanjiUseCase(resolve()) }
        provide<GetKanjiDetailsUseCase> { GetKanjiDetailsUseCase(resolve()) }

        provide<RegisterUserUseCase> { RegisterUserUseCase(resolve(), resolve()) }
        provide<LoginUserUseCase> { LoginUserUseCase(resolve(), resolve(), resolve()) }
        provide<GetCurrentUserUseCase> { GetCurrentUserUseCase(resolve()) }

        provide<GetLearningBlocksUseCase> { GetLearningBlocksUseCase(resolve()) }
        provide<GetLearningBlockDetailsUseCase> { GetLearningBlockDetailsUseCase(resolve()) }

        provide<GetWordProgressUseCase> { GetWordProgressUseCase(resolve(), resolve()) }
        provide<GetKanjiProgressUseCase> { GetKanjiProgressUseCase(resolve(), resolve()) }
        provide<UpdateWordProgressUseCase> { UpdateWordProgressUseCase(resolve(), resolve()) }
        provide<UpdateKanjiProgressUseCase> { UpdateKanjiProgressUseCase(resolve(), resolve()) }

        provide<GetReviewWordsUseCase> { GetReviewWordsUseCase(resolve(), resolve()) }
        provide<GetReviewKanjiUseCase> { GetReviewKanjiUseCase(resolve(), resolve()) }
        provide<CheckWordAnswerUseCase> { CheckWordAnswerUseCase(resolve(), resolve()) }
        provide<CheckKanjiAnswerUseCase> { CheckKanjiAnswerUseCase(resolve(), resolve()) }
    }
}
