package org.infinispan.query.backend;

import org.hibernate.search.spi.IndexingMode;
import org.hibernate.search.spi.SearchIntegrator;
import org.infinispan.commands.FlagAffectedCommand;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.Index;
import org.infinispan.context.InvocationContext;
import org.infinispan.context.impl.FlagBitSets;
import org.infinispan.distribution.DistributionInfo;
import org.infinispan.distribution.DistributionManager;
import org.infinispan.remoting.rpc.RpcManager;

/**
 * Defines for which events the Query Interceptor will generate indexing events.
 *
 * @author Sanne Grinovero &lt;sanne@hibernate.org&gt; (C) 2014 Red Hat Inc.
 * @since 7.0
 */
public enum IndexModificationStrategy {

   /**
    * Indexing events will not be triggered automatically, still the indexing service will be available. Suited for
    * example if index updates are controlled explicitly.
    */
   MANUAL {
      @Override
      public boolean shouldModifyIndexes(FlagAffectedCommand command, InvocationContext ctx,
                                         DistributionManager distributionManager, RpcManager rpcManager, Object key) {
         return false;
      }
   },

   /**
    * Any event intercepted by the current node will trigger an indexing event (excepting those flagged with {@code
    * Flag.SKIP_INDEXING}.
    */
   ALL {
      @Override
      public boolean shouldModifyIndexes(FlagAffectedCommand command, InvocationContext ctx,
                                         DistributionManager distributionManager, RpcManager rpcManager, Object key) {
         if (key == null || distributionManager == null) {
            return true;
         }
         DistributionInfo info = distributionManager.getCacheTopology().getDistribution(key);
         // If this is a backup node we should modify the entry in the remote context
         return info.isPrimary() || info.isWriteOwner() &&
               (ctx.isInTxScope() || !ctx.isOriginLocal() || command != null && command.hasAnyFlag(FlagBitSets.PUT_FOR_STATE_TRANSFER));
      }
   },

   /**
    * Only events target to the primary owner will trigger indexing of the data
    */
   PRIMARY_OWNER {
      @Override
      public boolean shouldModifyIndexes(FlagAffectedCommand command, InvocationContext ctx,
                                         DistributionManager distributionManager, RpcManager rpcManager, Object key) {
         if (key == null) {
            return ctx.isOriginLocal();
         }
         return (command == null || !command.hasAnyFlag(FlagBitSets.PUT_FOR_STATE_TRANSFER)) &&
               (distributionManager == null || distributionManager.getCacheTopology().getDistribution(key).isPrimary());

      }
   };

   public abstract boolean shouldModifyIndexes(FlagAffectedCommand command, InvocationContext ctx,
                                               DistributionManager distributionManager, RpcManager rpcManager, Object key);

   /**
    * For a given configuration, define which IndexModificationStrategy is going to be used.
    *
    * @param searchFactory
    * @param cfg
    * @return the appropriate IndexModificationStrategy
    */
   public static IndexModificationStrategy configuredStrategy(SearchIntegrator searchFactory, Configuration cfg) {
      IndexingMode indexingMode = searchFactory.unwrap(SearchIntegrator.class).getIndexingMode();
      if (indexingMode == IndexingMode.MANUAL) {
         return MANUAL;
      } else {
         if (cfg.indexing().index() == Index.PRIMARY_OWNER) {
            return PRIMARY_OWNER;
         } else {
            return ALL;
         }
      }
   }
}