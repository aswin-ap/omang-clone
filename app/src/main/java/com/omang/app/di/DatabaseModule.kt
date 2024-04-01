package com.omang.app.di


import android.content.Context
import com.omang.app.data.database.OmangDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext appContext: Context): OmangDatabase =
        OmangDatabase.getDatabase(appContext)

    @Singleton
    @Provides
    fun provideUserInfoDao(db: OmangDatabase) = db.userInfoDao()

    @Singleton
    @Provides
    fun provideMyWebPlatformDao(db: OmangDatabase) = db.myWebPlatformDao()

    @Singleton
    @Provides
    fun provideMyClassroomDao(db: OmangDatabase) = db.myClassroomDao()

    @Singleton
    @Provides
    fun provideContentContentDao(db: OmangDatabase) = db.contentContentDao()

    @Singleton
    @Provides
    fun provideResourcesDao(db: OmangDatabase) = db.resourcesDao()

    @Singleton
    @Provides
    fun provideUnitDao(db: OmangDatabase) = db.unitDao()

    @Singleton
    @Provides
    fun provideLessonDao(db: OmangDatabase) = db.lessonDao()

    @Singleton
    @Provides
    fun provideClassroomResourcesRelationDao(db: OmangDatabase) = db.classroomResourcesRelationDao()

    @Singleton
    @Provides
    fun provideUnitLessonRelationDao(db: OmangDatabase) = db.unitLessonRelationDao()

    @Singleton
    @Provides
    fun provideClassroomLessonRelationDao(db: OmangDatabase) = db.classroomLessonRelationDao()

    @Singleton
    @Provides
    fun provideClassroomWebsiteRelationDao(db: OmangDatabase) = db.classroomWebsiteRelationDao()

    @Singleton
    @Provides
    fun provideLessonWeblinksMappingDao(db: OmangDatabase) = db.lessonWeblinksMappingDao()

    @Provides
    fun provideNotificationDao(db: OmangDatabase) = db.notificationDao()

    @Singleton
    @Provides
    fun provideTestDao(db: OmangDatabase) = db.testDao()

    @Singleton
    @Provides
    fun provideUnitTestDao(db: OmangDatabase) = db.unitTestDo()

    @Singleton
    @Provides
    fun provideQuestionDao(db: OmangDatabase) = db.questionDao()

    @Singleton
    @Provides
    fun provideTicketsDao(db: OmangDatabase) = db.ticketsDao()

    @Singleton
    @Provides
    fun provideNavigationDao(db: OmangDatabase) = db.mobileNavDao()

    @Singleton
    @Provides
    fun provideLocationDao(db: OmangDatabase) = db.locationDao()

    @Singleton
    @Provides
    fun provideModeMeterDao(db: OmangDatabase) = db.modeMeterDao()

    @Singleton
    @Provides
    fun provideMoodDataDao(db: OmangDatabase) = db.modeDataDao()

    @Singleton
    @Provides
    fun providePsmDao(db: OmangDatabase) = db.psmDao()

    @Singleton
    @Provides
    fun provideDeviceAnalyticsDao(db: OmangDatabase) = db.deviceAnalyticsDao()

    @Singleton
    @Provides
    fun provideClassroomFeedDao(db: OmangDatabase) = db.classroomFeedDao()

    @Singleton
    @Provides
    fun provideFileDao(db: OmangDatabase) = db.fileDao()
}
