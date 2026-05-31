package weberstudio.app.billigsteprodukter

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import weberstudio.app.billigsteprodukter.data.AppDatabase
import java.io.IOException

/**
 * Validerer Room-migrations mod de eksporterede skemaer i `app/schemas`.
 *
 * Version 11 er baseline (det første eksporterede skema). Hver gang databasen bumpes,
 * skal der tilføjes en migration i [AppDatabase] OG en linje her, der migrerer fra den
 * forrige version til den nye via [MigrationTestHelper.runMigrationsAndValidate].
 *
 * Indtil den første rigtige migration (11 -> 12) findes, sikrer testen blot at baseline-
 * skemaet kan oprettes og åbnes — så fundamentet er på plads.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val testDb = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    /**
     * Opretter baseline-databasen (version 11) ud fra det eksporterede skema og lukker den.
     * Fejler hvis `app/schemas/.../11.json` mangler eller ikke matcher entiteterne.
     */
    @Test
    @Throws(IOException::class)
    fun baselineSchemaOpens() {
        helper.createDatabase(testDb, 11).apply { close() }
    }

    // Når databasen bumpes til 12, tilføjes en tilsvarende test her, fx:
    //
    // @Test
    // @Throws(IOException::class)
    // fun migrate11To12() {
    //     helper.createDatabase(testDb, 11).apply {
    //         // indsæt evt. testdata via execSQL
    //         close()
    //     }
    //     helper.runMigrationsAndValidate(testDb, 12, true, MIGRATION_11_12)
    // }
}
