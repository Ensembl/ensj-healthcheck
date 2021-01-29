-- Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
-- Copyright [2016-2021] EMBL-European Bioinformatics Institute
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

-- Remove manual_ok_all_releases option
-- This option is too dangerous and could hide important HC warnings
-- If a warning is not relevant, the HC should be updated to stop warning


ALTER TABLE annotation MODIFY COLUMN action enum('manual_ok','under_review','fixed','note','healthcheck_bug','manual_ok_this_assembly','manual_ok_this_genebuild','manual_ok_this_regulatory_build');
