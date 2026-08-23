package dev.celerbi.easyfarmersdelightcompat.compat.jade;

/**
 * Compatibility tombstone for the pre-1.2.0 source name.
 *
 * <p>The live provider is {@link FarmerHarvestToolJadeProvider}. The historical
 * Jade UID remains {@code farmer_knife} inside that provider so user-side Jade
 * preferences do not reset. This class intentionally implements no Jade API and
 * is never registered.</p>
 */
@Deprecated(forRemoval = true)
final class FarmerKnifeJadeProvider {
    private FarmerKnifeJadeProvider() {
    }
}
