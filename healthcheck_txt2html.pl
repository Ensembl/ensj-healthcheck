#!/usr/bin/env perl
# Copyright [2017-2019] EMBL-European Bioinformatics Institute
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.



=head1 CONTACT

  Please email comments or questions to the public Ensembl
  developers list at <http://lists.ensembl.org/mailman/listinfo/dev>.

  Questions may also be sent to the Ensembl help desk at
  <helpdesk.org>.

=cut


# Script to convert the healthcheck reports written in simple text file into an HTML file.

use strict;
use warnings;
use Getopt::Long;

# Print the usage instructions if run without parameters
usage() unless (scalar(@ARGV));

my ($input_file,$output_file,$version,$help);

GetOptions(
  'i=s'         => \$input_file,
  'o=s'         => \$output_file,
  'v=i'         => \$version,
  'help!'       => \$help,
);

usage("Input and output files must be specified, using the options '-o' and '-i'") unless ($input_file && $output_file);
usage("A release version must be specified, using the option '-v'") unless ($version);

my %test_case_groups;
my %test_cases;
my %status_by_db;
my %sp_alias;
my $results_by_test_case = 0;
my $current_test_case;
my $current_db_name;


my $btn_id_suffix = 'button';
my $div_id_suffix = 'content';
my $hc_success = 'PASSED';
my $hc_failed  = 'FAILED';
my %status_colour = ( $hc_success => 'label-success',
                      $hc_failed  => 'label-danger',
                      'default'   => 'label-info'
                    );

my %special_testcase = ( 'CompareVariationSchema' => 1,
                         'ForeignKeyCoreId'       => 1
                       );

my $pre_colour = 'pre_colour';
my $new_colour = 'new_colour';
my $err_colour = 'err_colour';
my $span_open_tag = '<span class="bold_font ';

my $skipped_test_text = "<span class=\"bold_font err_colour\">Skipped test</span>: The test died with an exception!<br/>Please try to run the HealthChecks again.";

my @key_words = ('from', 'seq_region', 'entries in', 'in variation set', 'consequence type', 'variations having at least one evidence annotation');
my $row_id = 1;

open HC, "< $input_file" or die $!;
while (<HC>) {
  chomp $_;
  next if ($_ eq '' || $_ =~ /^\./);
  
  # Test case results
  if ($results_by_test_case) {
    last if ($_ =~ /^--/);
    # Test case header
    if ($_ =~ /^org.ensembl.healthcheck/) {
      my $tc_lib = (split(' ',$_))[0];
      my @tc_path = split(/\./, $tc_lib);
      $current_test_case = $tc_path[$#tc_path];
      
      # Extra generic test case from production:
      if ($current_db_name && $current_test_case eq 'TestRunnerSelfCheck') {
        $test_case_groups{$current_db_name}{$current_test_case} = 'FAILED';
      }
    }
    # Test case detailled row
    else {
      my @tc_result = split(': ', $_, 2);
      my $db_name = $tc_result[0];
         $db_name =~ s/ //g;
      my $tc_res;
      if ($db_name =~ /^(\w+)_(\w+)_variation_$version\_/) {
        my $sp_alias_1 = substr($1,0,1);
        my $sp_alias_2 = $2;
        $sp_alias{$db_name} = lc($sp_alias_1.$sp_alias_2);
        $current_db_name = $db_name;
        $tc_res = $tc_result[1];
      }
      else {
        $db_name = $current_db_name;
        $sp_alias{$db_name} = $db_name;
        $tc_res = $_;
        $tc_res =~ s/^\s+//;
      }
      
      ## Compare versions ##
      my ($previous_count,$new_count,$item,$feature,$short_report);
      # New count
      if ($tc_res =~ /has none/) {
        $new_count = 'none';
      }
      elsif ($tc_res =~ /only has ([0-9]+)/) {
        $new_count = $1;
      }
      
      # Previous count + feature + item
      if ($tc_res =~ /has ([0-9]+) number of /) {
        $previous_count = $1;
        foreach my $key_word (@key_words) {
          if ($tc_res =~ /number of (.+) $key_word (.+) but/) {
            $feature = $1;
            $item = $2;
          }
          elsif ($tc_res =~ /number of $key_word (.+) but/) {
            $feature = $key_word;
            $item = $1;
          }
        }
      }
      elsif ($tc_res =~ /has ([0-9]+) /) {
        $previous_count = $1;
        foreach my $key_word (@key_words) {
          if ($tc_res =~ /has [0-9]+ $key_word (.+) but/) {
            $feature = $key_word;
            $item = $1;
          }
        }
      }
      
      # Formatting short report row
      if ($previous_count && $new_count && $item && $feature) {
        $previous_count = thousandify($previous_count);
        $previous_count =~ s/([0-9]+)/$span_open_tag$pre_colour"\>$1\<\/span\>/g;
        $new_count = thousandify($new_count);
        $new_count      =~ s/([0-9]+|none)/$span_open_tag$new_colour"\>$1\<\/span\>/g;
        $item           = "<b>$item</b>";
        $short_report = 1;
      }
      
      # General text decorators
      $tc_res =~ s/has none/has $span_open_tag$new_colour"\>none\<\/span\>/;
      $tc_res =~ s/FAILURE DETAILS: ([0-9]+)/FAILURE DETAILS: $span_open_tag$err_colour"\>$1\<\/span\>/;
      $tc_res =~ s/has ([0-9]+) /has $span_open_tag$pre_colour"\>$1\<\/span\> /;
      $tc_res =~ s/only has ([0-9]+)/only has $span_open_tag$new_colour"\>$1\<\/span\>/;
      $tc_res =~ s/^([0-9]+)/$span_open_tag$err_colour"\>$1\<\/span\>/;
      $tc_res = thousandify($tc_res);
      $tc_res =~ s/Table `(.+)` exists/Table \<b\>`$1`\<\/b\> exists/g;
      foreach my $item (@key_words) {
        $tc_res =~ s/$item (.+) but/$item \<b\>$1\<\/b\> but/g;
      }
      
      # Generate row reports
      if ($short_report) {
        my $div_id = $sp_alias{$db_name}."_".lc($current_test_case)."_".$row_id;
        $row_id ++;
           
        $tc_res = qq{
          <div>
            <button id="$div_id\_$btn_id_suffix" class="glyphicon glyphicon-plus-sign showhide_hc" onclick="showhide_row('$div_id')"></button> 
            <span>$feature $item : $previous_count / $new_count</span>
          </div>
          <div id="$div_id\_$div_id_suffix" class="hc_extra_row_div" style="display:none">
            $tc_res
          </div>
        };
      }
      
      # Add row reports
      if ($test_cases{$db_name}{$current_test_case}) {
        push(@{$test_cases{$db_name}{$current_test_case}},$tc_res);
      }
      else {
        $test_cases{$db_name}{$current_test_case} = [$tc_res];
        # Missing the catch of some HCs when multi DB HCs run is done
        if ($special_testcase{$current_test_case} && !$test_case_groups{$current_db_name}{$current_test_case}){
          $test_case_groups{$current_db_name}{$current_test_case} = $hc_failed;
          $status_by_db{$current_db_name}{$hc_failed} ++;
        }
      }
    }
  }
  # Test case summary results (i.e. top table)
  else {
    if ($_ =~ /RESULTS BY TEST CASE/) {
      $results_by_test_case = 1;
      next;
    }
    my ($test_case,$db_name,$status);
    if ($_ =~ /\[/) {
      # 2 reports on the same line
      if ($_ =~ /^(\w+\s\[\w+\])\s(\w+\s\[\w+\].+)$/) {
        my @entries = ($1,$2);
        foreach my $entry (@entries) {
          if ($entry =~ /\]\s+\./) {
            ($test_case,$db_name,$status) = $entry =~ /^(\w+)\s\[(.+)\]\s+\.*\s?(\w+)$/;
          }
          else {
            ($test_case,$db_name) = $entry =~ /^(\w+)\s\[(.+)\]/;
            $status = 'FAILED';
            if ($test_cases{$db_name}{$test_case}) {
              push(@{$test_cases{$db_name}{$test_case}}, $skipped_test_text);
            }
            else {
              $test_cases{$db_name}{$test_case} = [$skipped_test_text];
            }
          }
          $test_case_groups{$db_name}{$test_case} = $status;
          $current_db_name = $db_name;
        }
      }
      # Just one report per line
      else {
        ($test_case,$db_name,$status) = $_ =~ /^(\w+)\s\[(.+)\]\s+\.*\s?(\w+)$/;
        $test_case_groups{$db_name}{$test_case} = $status;
        $current_db_name = $db_name;
      }
    }
    else {
      ($test_case,$status) = $_ =~ /^(\w+)\s\.*\s?(\w+)$/;
      $test_case_groups{$current_db_name}{$test_case} = $status;
    }
    
    $status_by_db{$current_db_name}{$status} ++;
  }
}
close(HC);

sub add_spaces {
    my $tc = shift;
    my $tc_label = $tc;
       $tc_label =~ s/([A-Z])/ $1/g;
       foreach my $known_name (qw(MLSS MSA MySQL EG VB ID KB EFO CAFE ENA DB CCDS HGNC LRG MIM HTML GO UTR EST DNA MT GERP)) {
           my $wide_name = $known_name;
           $wide_name =~ s/([A-Z])/ $1/g;
           $wide_name =~ s/^\s+//;
           $tc_label =~ s/$wide_name/$known_name/;
       }
       $tc_label =~ s/^\s+//;
    return $tc_label;
}


#################
#  Output file  #
#################

my $head_html = get_html_head();

open RESULTS, "> $output_file" or die $!;
print RESULTS qq{
<html>$head_html
  <body style="padding: 5px 8px">
};

my $several_dbs = (scalar(keys(%test_case_groups)) > 1) ? 1 : 0;
my $hc_header_icon = ($several_dbs) ? 'glyphicon-menu-right' : 'glyphicon-menu-down';
my $hc_content_display = ($several_dbs) ? ' style="display:none"' : '';

my $date_time = get_run_date_time();

foreach my $db_name (sort(keys(%test_case_groups))) {
  # Species header
  my $status_html = '';
  # List of status with their counts
  foreach my $status (sort{ ($a ne $hc_success) cmp ($b ne $hc_success) || $a cmp $b } keys(%{$status_by_db{$db_name}})) {
    my $status_c = $status_colour{$status} ? $status_colour{$status} : $status_colour{'default'};
    $status_html .= sprintf( qq{<span class="label hc_status hc_status_left hc_font_1">%i</span><span class="label %s hc_status_right hc_font_1">%s</span>},
                             $status_by_db{$db_name}{$status}, $status_c, $status
                           );
  }
  my $db_top_anchor = $sp_alias{$db_name}."_top";
  print RESULTS qq{
    <a id="$db_top_anchor"></a>
    <div class="db_header clearfix">
      <div class="left db_header_left">
        <button id="$db_name\_button" class="glyphicon $hc_header_icon showhide_hc" onclick="showhide('$db_name', 1)"></button>
        <span>$db_name</span>
      </div>
      <div class="left db_header_middle">$status_html</div>
      <div class="right db_header_right" data-toggle="tooltip" data-placement="bottom" title="HC finished date | time" ><span class="glyphicon glyphicon-time"></span>$date_time</div>
    </div>
    <div id="$db_name\_content" class="db_content"$hc_content_display>
      <div class="clearfix">\n};
  
  # Passed successfully healtChecks list
  if ($status_by_db{$db_name}{$hc_success}) {
    print RESULTS get_hc_list($db_name, $hc_success);
  }
  
  # Other healtChecks list
  if ($status_by_db{$db_name}{$hc_failed}) {
    print RESULTS get_hc_list($db_name, $hc_failed);
  }
  
  # Colour legend
  print RESULTS qq{
        <div class="legend_box">
          <div class="bold_font">Colour legend</div>
          <div><span class="$pre_colour">123456</span><span>: number of entries in the previous release</span></div>
          <div><span class="$new_colour">123456</span><span>: number of entries in the new release</span></div>
          <div><span class="$err_colour">123456</span><span>: number of entries in the error report</span></div>
        </div>
      </div>
  };
  
  # HealtChecks results
  foreach my $tc (sort(keys(%{$test_case_groups{$db_name}}))) {
    my $status = $test_case_groups{$db_name}{$tc};
    next if ($status eq $hc_success);
    my $status_c = $status_colour{$status} ? $status_colour{$status} : $status_colour{'default'};
    my $tc_label = add_spaces($tc);
       $tc_label =~ s/Previous Version/Previous Version \-/i;
    my $tc_id = $sp_alias{$db_name}."_".lc($tc);
    my $button_id = $tc_id."_".$btn_id_suffix;

    if ($test_cases{$db_name}{$tc} && scalar(@{$test_cases{$db_name}{$tc}})) {
      print RESULTS sprintf(
        qq{
    <div id="%s" class="hc_header">
      <button id="%s" class="glyphicon glyphicon-menu-down showhide_hc" onclick="showhide('%s')"></button>
     %s<span class="label %s hc_status hc_font_1">%s</span>
      <div class="hc_top_link hc_font_1"><a href="#%s"><span class="glyphicon glyphicon-circle-arrow-up"></span> top</a></div>
    </div>\n},
        $tc_id, $button_id, $tc_id, $tc_label, $status_c, $status, $sp_alias{$db_name}."_top"
      );
    
      my $content_id = "$tc_id\_$div_id_suffix";
      print RESULTS qq{    <div id="$content_id">
      <table class="table table-hover hc_font_1"><tbody>\n};
      foreach my $result (@{$test_cases{$db_name}{$tc}}) {
        print RESULTS qq{        <tr><td>$result</td></tr>\n};
      }
      print RESULTS qq{      </tbody></table>\n    </div>\n};
    }
  }
  print RESULTS qq{</div>};
}
print RESULTS qq{</body></html>};


sub get_hc_list {
  my $db_name = shift;
  my $type    = shift;

  my $html = '';

  my $count_col_header = '';
  my $margin_left      = '';

  my $id = "$db_name\_$type";

  my $glyph_class = ($type eq $hc_success) ? 'glyphicon-menu-right' : 'glyphicon-menu-down';
  my $display_table = ($type eq $hc_success) ? ' style="display:none"' : '';
  if ($type ne $hc_success) {
    $count_col_header = qq{\n                <th>Count</th>};
    $margin_left = ' style="margin-left:30px"' if ($status_by_db{$db_name}{$hc_success});
  }

  my $lc_type = lc($type);
  $html .=  qq{
        <div class="left"$margin_left">
          <div class="header_hc_list header_hc_list_$lc_type">
            <button id="$id\_$btn_id_suffix" class="glyphicon $glyph_class showhide_hc" onclick="showhide('$id')"></button> HealthCheck <span>$type</span>
          </div>
          <table class="table table-hover table_hc_list" id="$id\_$div_id_suffix"$display_table>
            <thead>
              <tr>
                <th>HealthCheck</th>
                <th>Status</th>$count_col_header
              </tr>
            </thead>
            <tbody>\n};
            
  foreach my $tc (sort(keys(%{$test_case_groups{$db_name}}))) {
    my $status = $test_case_groups{$db_name}{$tc};
    
    next if ($status ne $hc_success && $type eq $hc_success); # HC success table
    next if ($status eq $hc_success && $type ne $hc_success); # HC failed table
      
    my $tc_title = '';
      
    my $status_c = $status_colour{$status} ? $status_colour{$status} : $status_colour{'default'};
    my $tc_label = add_spaces($tc);
       if ($tc_label =~ /Previous Version/i) {
         $tc_title = qq{ class="hc_help" data-toggle="tooltip" data-placement="right" title="$tc_label"};
         $tc_label =~ s/Previous Version/\.\.\./i;
       }

    my $hc_errors_count = '';
    if ($type ne $hc_success) {
      $hc_errors_count = ($test_cases{$db_name}{$tc} && scalar(@{$test_cases{$db_name}{$tc}})) ? scalar(@{$test_cases{$db_name}{$tc}}) : '-';
      $hc_errors_count = qq{
                  <td class="row_count" title="Number of errors reported">$hc_errors_count</td>};
      $html .= sprintf( qq{              <tr>
                  <td><a href="#%s"$tc_title>%s</a></td>
                  <td style="vertical-align:middle"><span class="label %s">%s</td>%s
                </tr>\n},
                $sp_alias{$db_name}."_".lc($tc), $tc_label, $status_c, $status, $hc_errors_count
               );
    }
    else {
      $html .= sprintf( qq{              <tr>
                  <td><span$tc_title>%s</span></td>
                  <td style="vertical-align:middle"><span class="label %s">%s</td>
                </tr>\n},
                $tc_label, $status_c, $status
               );
    }
  }
  $html .=  qq{          </tbody>
          </table>
        </div>};

  return $html;
}

sub thousandify {
  my $number = shift;
  my $text = reverse $number;
  $text =~ s/(\d\d\d)(?=\d)(?!\d*\.)/$1,/gi;
  return scalar reverse $text;
}

sub get_run_date_time {
  my $time_line = `grep 'Results reported' $input_file`;
  
  if ($time_line) {
    $time_line =~ /Results\sreported\sat\s\w+\s(\w+)\s+(\d+)\s(\d+:\d+:\d+)\s(\d+)/;
    my $month = $1;
    my $day   = $2;
    my $time  = $3;
    my $year  = $4;
    
    return qq{<span>$day $month $year | $time</span>};
  }
  
}

sub get_html_head {
 return qq{
  <head>
    <title>HealthCheck results</title>
    <!-- CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css">
    <style type="text/css">
      .db_header { background-color:#555; color: #FFF; padding:5px 6px; margin:5px 0px 10px; font-size:22px; }
      .db_header_left  { line-height:32px;vertical-align:middle;width:500px; }
      .db_header_right>button { vertical-align:middle; }
      .db_header_right>span   { vertical-align:middle; }
      .db_header_middle>span { line-height:32px;vertical-align:middle; }
      .db_header_right { line-height:32px;vertical-align:middle;margin-right:15px;font-size:12px; }
      .db_header_right>span:first-child { vertical-align:middle;padding-right:5px;top:0px; }
      .db_header_right>span:last-child { vertical-align:middle; }
      .left { float:left; }
      .right { float:right; }
      .clearfix:after { clear:both; }
      .db_content { padding: 0px 15px 50px; }
      .header_hc_list { padding:4px 6px;font-size 14px;font-weight:bold;background-color:#EEE;margin-bottom:5px; }
      .header_hc_list_passed { border-bottom:2px solid #5cb85c;}
      .header_hc_list_passed > span { color:#5cb85c;}
      .header_hc_list_failed { border-bottom:2px solid #d9534f;}
      .header_hc_list_failed > span { color:#d9534f;}
      .table_hc_list { font-size:12px; margin-bottom:15px; max-width:375px; }
      .table_hc_list>thead>tr { background-color:#EEE; }
      .table_hc_list>thead>tr>th { padding:4px 5px; border-top:1px solid #DDD; }
      .table_hc_list>tbody>tr>td { padding:2px; }
      .hc_help { cursor:pointer; }
      .legend_box { float:right; padding:5px; border:1px solid #CCC; }
      .bold_font { font-weight:bold; }
      .hc_header { padding:4px 6px; font-size:20px; margin:30px 0px 5px; background-color:#EEE; }
      .showhide_hc { border:none; background-color:transparent; vertical-align:text-bottom; }
      .showhide_hc:hover { color: #00F; }
      .hc_header_icon { padding:3px 6px; border:none; background-color:transparent; vertical-align:text-bottom; }
      .hc_status { margin-left:25px; vertical-align:middle; }
      .hc_status_left  { color:#000; background-color:#FFF; border-radius: 0.25em 0 0 0.25em; }
      .hc_status_right { vertical-align:middle; border-radius: 0 0.25em 0.25em 0; }
      .hc_top_link { float:right; }
      .hc_top_link a { text-decoration:none; padding:4px; }
      .hc_extra_row_div { padding:4px 0px; margin-left:30px; }
      .hc_font_1 { font-size:12px; }
      .pre_colour { color:#00D; }
      .new_colour { color:#ff8000; }
      .err_colour { color:#D00; }
      .black { color:#000; }
      .row_count { text-align:right; }
    </style>
    
    <!-- Javascript -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.2/jquery.min.js"></script>
    <script type="text/javascript" src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>
    <script type="text/javascript">
      \$(document).ready(function(){
        // Popups
        \$('[data-toggle="tooltip"]').tooltip();
      });
      
      function showhide(id, long) {
        var div_id = "#"+id+"_$div_id_suffix";
        var button_id = "#"+id+"_$btn_id_suffix";
        
        var duration = 150;
        if (long) {
          duration = 300;
        }
        
        if(\$(button_id).hasClass("glyphicon-menu-right")) {
          \$(button_id).removeClass("glyphicon-menu-right").addClass("glyphicon-menu-down");
          \$(div_id).show(duration);
        }
        else {
          \$(button_id).removeClass("glyphicon-menu-down").addClass("glyphicon-menu-right");
          \$(div_id).hide(duration);
        }
      }
      
      function showhide_row(id) {
        var div_id = "#"+id+"_$div_id_suffix";
        var button_id = "#"+id+"_$btn_id_suffix";
        
        if(\$(button_id).hasClass("glyphicon-plus-sign")) {
          \$(button_id).removeClass("glyphicon-plus-sign").addClass("glyphicon-minus-sign");
          \$(div_id).show(150);
        }
        else {
          \$(button_id).removeClass("glyphicon-minus-sign").addClass("glyphicon-plus-sign");
          \$(div_id).hide(150);
        }
      }
    </script>
  </head>
};
}

sub usage {
  my $msg = shift;
  print qq{
  $msg
  Usage: perl healthcheck_txt2html.pl [OPTIONS]
  
  Script to convert the healthcheck reports written in simple text file into an HTML file.
  
  Options:

    -help           Print this message
      
    -v              Ensembl release version, e.g. 88 (Required)
    -i              Path to the healthcheck reports text file (Required)
    -o              An HTML output file name (Required)
  } . "\n";
  exit(0);
}
